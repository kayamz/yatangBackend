package com.kaya.yatang.controller;

import com.kaya.yatang.dto.recipe.AiRecipeEnvelopeDto;
import com.kaya.yatang.dto.recipe.AiSuggestQuotaDto;
import com.kaya.yatang.dto.recipe.RecipeSuggestRequest;
import com.kaya.yatang.service.AiSuggestQuotaService;
import com.kaya.yatang.service.OpenAiRecipeService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeAiController {

    private final OpenAiRecipeService openAiRecipeService;
    private final AiSuggestQuotaService aiSuggestQuotaService;

    /** 오늘(클라이언트 로컬 날짜) AI 추천 사용량 조회 */
    @GetMapping("/suggest-quota")
    public ResponseEntity<AiSuggestQuotaDto> suggestQuota(
            Authentication authentication,
            @RequestHeader(value = "X-Guest-Session-Id", required = false) String guestSessionId,
            @RequestHeader(value = "X-Client-Local-Date", required = false) String clientLocalDate) {
        LocalDate usageDate = aiSuggestQuotaService.parseClientLocalDate(clientLocalDate);
        return ResponseEntity.ok(aiSuggestQuotaService.getQuota(authentication, guestSessionId, usageDate));
    }

    /** OpenAI 기반 레시피 3개 추천 (JSON) */
    @PostMapping("/suggest")
    public ResponseEntity<AiRecipeEnvelopeDto> suggest(
            @RequestBody RecipeSuggestRequest request,
            Authentication authentication,
            @RequestHeader(value = "X-Guest-Session-Id", required = false) String guestSessionId,
            @RequestHeader(value = "X-Client-Local-Date", required = false) String clientLocalDate) {
        LocalDate usageDate = aiSuggestQuotaService.parseClientLocalDate(clientLocalDate);
        aiSuggestQuotaService.assertCanSuggest(authentication, guestSessionId, usageDate);
        AiRecipeEnvelopeDto envelope = openAiRecipeService.suggestRecipes(request);
        AiSuggestQuotaDto quota =
                aiSuggestQuotaService.recordSuccessfulSuggest(authentication, guestSessionId, usageDate);
        envelope.setSuggestQuota(quota);
        return ResponseEntity.ok(envelope);
    }
}
