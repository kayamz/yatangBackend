package com.kaya.yatang.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kaya.yatang.config.OpenAiProperties;
import com.kaya.yatang.dto.recipe.AiRecipeEnvelopeDto;
import com.kaya.yatang.dto.recipe.RecipeIngredientLine;
import com.kaya.yatang.dto.recipe.RecipeSuggestRequest;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class OpenAiRecipeService {

    private static final Logger log = LoggerFactory.getLogger(OpenAiRecipeService.class);

    private final OpenAiProperties openAiProperties;
    private final OpenAiRecipeSuggestConcurrencyGate recipeSuggestConcurrencyGate;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    public AiRecipeEnvelopeDto suggestRecipes(RecipeSuggestRequest request) {
        if (openAiProperties.getApiKey() == null || openAiProperties.getApiKey().isBlank()) {
            throw new IllegalStateException(
                    "OpenAI API 키가 설정되지 않았습니다. 환경변수 OPENAI_API_KEY 또는 yatang.openai.api-key 를 설정하세요.");
        }
        if (request.getIngredients() == null || request.getIngredients().isEmpty()) {
            throw new IllegalArgumentException("보유 재료 목록이 비어 있습니다.");
        }

        List<String> recent = sanitizeRecentRecipeTitles(request.getRecentRecipeTitles());
        String userMessage = buildUserMessage(request.getIngredients(), recent);
        final String body;
        try {
            body = buildChatRequestBody(userMessage);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("요청 본문 생성 실패", e);
        }

        // 서비스 관리자가 서버 로그에서 GPT에 보내는 내용을 확인할 수 있도록 (API 키는 요청 본문에 포함되지 않음)
        log.info("[OpenAI 레시피][관리자용] 사용자 재고 기반 user 메시지:\n{}", userMessage);
        try {
            String pretty = objectMapper.readTree(body).toPrettyString();
            log.info("[OpenAI 레시피][관리자용] Chat Completions 전송 JSON (model·messages·temperature 등):\n{}", pretty);
        } catch (Exception e) {
            log.info("[OpenAI 레시피][관리자용] Chat Completions 전송 본문(파싱 실패 시 원문):\n{}", body);
        }

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(openAiProperties.getBaseUrl().replaceAll("/$", "") + "/chat/completions"))
                .header("Authorization", "Bearer " + openAiProperties.getApiKey().trim())
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(120))
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        recipeSuggestConcurrencyGate.enter();
        try {
            HttpResponse<String> response =
                    httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException(
                        "OpenAI 호출 실패 (HTTP " + response.statusCode() + "): " + truncate(response.body(), 500));
            }
            String json = extractMessageContentJson(response.body());
            AiRecipeEnvelopeDto envelope = objectMapper.readValue(json, AiRecipeEnvelopeDto.class);
            if (envelope.getRecipes() == null) {
                envelope.setRecipes(List.of());
            }
            if (envelope.getRecipes().size() > 3) {
                envelope.setRecipes(envelope.getRecipes().subList(0, 3));
            }
            return envelope;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("레시피 생성 중 오류: " + e.getMessage(), e);
        } finally {
            recipeSuggestConcurrencyGate.leave();
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    private static List<String> sanitizeRecentRecipeTitles(List<String> raw) {
        if (raw == null || raw.isEmpty()) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (String t : raw) {
            if (t == null) {
                continue;
            }
            String s = t.trim().replaceAll("\\s+", " ");
            if (s.isEmpty()) {
                continue;
            }
            if (s.length() > 80) {
                s = s.substring(0, 80);
            }
            out.add(s);
            if (out.size() >= 30) {
                break;
            }
        }
        return out;
    }

    private String buildUserMessage(List<RecipeIngredientLine> lines, List<String> recentTitles) {
        String list = lines.stream()
                .map(l -> {
                    String q = l.getQuantity() != null ? String.valueOf(l.getQuantity()) : "?";
                    String u = l.getUnit() != null ? l.getUnit() : "";
                    return "- " + l.getName() + ": " + q + u;
                })
                .collect(Collectors.joining("\n"));
        StringBuilder sb = new StringBuilder();
        sb.append("""
                아래는 사용자가 현재 보유한 식재료 목록입니다.

                %s

                이 재료들을 최대한 활용해 실제로 만들 수 있는 요리 레시피를 정확히 3개만 제안하세요.
                모든 재료가 있을 필요는 없으며, 없는 것은 missingIngredients에 적으세요.
                각 레시피의 ingredients(및 필요 시 missingIngredients) 항목마다 substitute 필드를 채우되,
                해당 재료를 흔히 바꿔 쓸 수 있는 경우에만 짧게 적고(예: 치킨스톡), 없으면 빈 문자열로 두세요.
                """
                .formatted(list));
        if (!recentTitles.isEmpty()) {
            String recentBlock = recentTitles.stream().map(t -> "- " + t).collect(Collectors.joining("\n"));
            sb.append("""


                    아래는 이 사용자가 최근에 AI로 받아 본 요리 이름입니다. 같은 제목은 쓰지 말고,
                    비슷한 스타일(예: 모두 진한 소스 볶음, 모두 크림 파스타)이라도 양념·주재료·조리 흐름이 겹치지 않게 3가지를 새로 제안하세요.

                    %s
                    """
                    .formatted(recentBlock));
        }
        return sb.toString();
    }

    private String buildChatRequestBody(String userMessage) throws JsonProcessingException {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", openAiProperties.getModel());
        root.put("temperature", 0.78);

        ObjectNode responseFormat = objectMapper.createObjectNode();
        responseFormat.put("type", "json_object");
        root.set("response_format", responseFormat);

        ArrayNode messages = objectMapper.createArrayNode();
        ObjectNode sys = objectMapper.createObjectNode();
        sys.put("role", "system");
        sys.put(
                "content",
                """
            당신은 한국에서 집밥을 자주 해 먹는 가정을 이해하는 요리 전문가입니다. 한식뿐 아니라 파스타·중식·일식·커리·샐러드 등 집에서 할 수 있는 요리 전반에 익숙합니다. 반드시 유효한 JSON만 출력합니다.

            출력 JSON 스키마:
            {
              "recipes": [
                {
                  "title": "요리 이름",
                  "cookMinutes": 정수(분),
                  "servings": 정수(인분),
                  "ingredients": [
                    {
                      "name": "재료명",
                      "amount": "n인분 기준 양(예: 200g, 1큰술)",
                      "note": "부가설명 또는 빈 문자열",
                      "substitute": "대체 가능 시 짧은 한글(예: 치킨스톡, 액젓). 대체가 없거나 불필요하면 빈 문자열"
                    }
                  ],
                  "steps": [ "번호 없이 단계 설명만", "다음 단계 설명만" ],
                  "missingIngredients": [
                    {
                      "name": "부족한 재료",
                      "amount": "필요량",
                      "note": "왜 필요한지 짧게",
                      "substitute": "구하기 어려울 때 대체 재료(짧게). 없으면 빈 문자열"
                    }
                  ],
                  "usesFromInventory": [ "보유 재고 활용 방식" ]
                }
              ]
            }

            규칙:
            - 반드시 JSON.parse 가능한 형식으로만 출력하세요.
            - 문자열 외의 설명, 코드블록, 주석을 포함하지 마세요.
            - 마지막 쉼표를 포함하지 마세요.

            - recipes 배열 길이는 정확히 3
            - 마트에서 재료를 구하기 쉬운 대중적인 집밥 위주로 제안할 것. 한식 비중은 자연스럽게 클 수 있으나, 세 요리를 모두 한식으로만 고정하지 말 것(재료가 허용하면 파스타·중식·일식·멕시칸·동남아 등 서로 다른 국가·스타일을 섞어 다양하게).
            - 한 번에 3개를 낼 때는 가능한 한 서로 다른 요리 계열이 되도록 하고, 과하게 뻔한 이름만 반복하지 말 것
            - 각 레시피는 최소 1~2개의 보유 재료를 반드시 사용
            - 실제로 조리 가능한 현실적인 요리만 제안
            - 일반 가정에서 가능한 조리법만 사용

            맛·레시피 품질:
            - 복잡한 셰프 요리가 아니라, 집에서 따라 하기 좋은 친절한 설명으로, 단계마다 불 세기·시간·순서를 구체적으로 적을 것(한 단계에 한 문장만 쓰지 말고, 필요하면 2~3문장)
            - 조리법에 맞게 감칠맛·단맛·산미·지방 향이 빠지지 않게 할 것(예: 한식 조림·볶음에는 설탕·물엿·맛술 등, 파스타에는 올리브유·치즈·마늘·토마토/크림, 중식 볶음에는 설탕·간장·샤오싱주·전분물 등 필요 시 ingredients에 명시). 없으면 missingIngredients에 적을 것.
            - 짠맛·단맛·감칠맛·산미 등 맛의 균형을 steps 안에서 어떻게 맞추는지 짧게라도 언급할 것
            - steps에 쓰인 조미·양념은 ingredients 목록과 일치해야 함(단계에서만 등장하는 재료는 누락 금지)

            - ingredients·missingIngredients 항목마다 substitute 필드를 포함할 것.
              재료마다 흔히 쓰는 대체재가 있으면 짧게 적고, 없으면 "".
              사용자가 넘긴 보유 재료와 맥락을 고려해 실용적으로 제안.
            - steps 배열 각 문자열에는 "1.", "2)", "1단계" 같은 번호를 넣지 말 것(UI가 순서를 붙임). 설명 문장만 작성(레시피당 통상 5~10단계)
            - amount는 g, ml, 큰술, 작은술, 컵, 개 등의 단위를 사용
            """
        );
        messages.add(sys);

        ObjectNode user = objectMapper.createObjectNode();
        user.put("role", "user");
        user.put("content", userMessage);
        messages.add(user);

        root.set("messages", messages);
        return objectMapper.writeValueAsString(root);
    }

    private String extractMessageContentJson(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        String content = root.path("choices")
                .path(0)
                .path("message")
                .path("content")
                .asText("");
        content = content.trim();
        if (content.startsWith("```")) {
            int nl = content.indexOf('\n');
            if (nl > 0) {
                content = content.substring(nl + 1);
            }
            int fence = content.lastIndexOf("```");
            if (fence > 0) {
                content = content.substring(0, fence).trim();
            }
        }
        return content;
    }
}
