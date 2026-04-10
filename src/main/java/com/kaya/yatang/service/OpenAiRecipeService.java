package com.kaya.yatang.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kaya.yatang.config.OpenAiProperties;
import com.kaya.yatang.dto.recipe.AiRecipeDto;
import com.kaya.yatang.dto.recipe.AiRecipeEnvelopeDto;
import com.kaya.yatang.dto.recipe.RecipeIngredientLine;
import com.kaya.yatang.dto.recipe.RecipeSuggestRequest;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OpenAiRecipeService {

    private final OpenAiProperties openAiProperties;
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

        String userMessage = buildUserMessage(request.getIngredients());
        final String body;
        try {
            body = buildChatRequestBody(userMessage);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("요청 본문 생성 실패", e);
        }

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(openAiProperties.getBaseUrl().replaceAll("/$", "") + "/chat/completions"))
                .header("Authorization", "Bearer " + openAiProperties.getApiKey().trim())
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(120))
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

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
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("레시피 생성 중 오류: " + e.getMessage(), e);
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    private String buildUserMessage(List<RecipeIngredientLine> lines) {
        String list = lines.stream()
                .map(l -> {
                    String q = l.getQuantity() != null ? String.valueOf(l.getQuantity()) : "?";
                    String u = l.getUnit() != null ? l.getUnit() : "";
                    return "- " + l.getName() + ": " + q + u;
                })
                .collect(Collectors.joining("\n"));
        return """
                아래는 사용자가 현재 보유한 식재료 목록입니다.

                %s

                이 재료들을 최대한 활용해 실제로 만들 수 있는 요리 레시피를 정확히 3개만 제안하세요.
                모든 재료가 있을 필요는 없으며, 없는 것은 missingIngredients에 적으세요.
                """.formatted(list);
    }

    private String buildChatRequestBody(String userMessage) throws JsonProcessingException {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", openAiProperties.getModel());
        root.put("temperature", 0.65);

        ObjectNode responseFormat = objectMapper.createObjectNode();
        responseFormat.put("type", "json_object");
        root.set("response_format", responseFormat);

        ArrayNode messages = objectMapper.createArrayNode();
        ObjectNode sys = objectMapper.createObjectNode();
        sys.put("role", "system");
        sys.put(
                "content",
                """
                        당신은 한국 가정식 요리 전문가입니다. 반드시 유효한 JSON만 출력합니다. 마크다운 코드블록을 쓰지 마세요.

                        출력 JSON 스키마:
                        {
                          "recipes": [
                            {
                              "title": "요리 이름",
                              "cookMinutes": 정수(분),
                              "servings": 정수(인분),
                              "ingredients": [ { "name": "재료명", "amount": "n인분 기준 양(예: 200g, 1큰술)", "note": "부가설명 또는 빈 문자열" } ],
                              "steps": [ "1단계 설명", "2단계 설명", ... ],
                              "missingIngredients": [ { "name": "부족한 재료", "amount": "필요량", "note": "왜 필요한지 짧게" } ],
                              "usesFromInventory": [ "보유 재고에서 이렇게 쓴다" ]
                            }
                          ]
                        }

                        규칙:
                        - recipes 배열 길이는 정확히 3
                        - 보유 재료와 무관한 요리는 제안하지 말 것 (최소 1~2개 이상 재고 연계)
                        - steps는 번호 순서대로 명확하게
                        - amount 필드는 한국어 단위(g, ml, 큰술, 개 등)를 사용해도 됨
                        """);
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
