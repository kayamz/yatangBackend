package com.kaya.yatang.controller;

import com.kaya.yatang.dto.recipe.AiRecipeEnvelopeDto;
import com.kaya.yatang.dto.recipe.RecipeSuggestRequest;
import com.kaya.yatang.service.OpenAiRecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeAiController {

    private final OpenAiRecipeService openAiRecipeService;

    /** OpenAI 기반 레시피 3개 추천 (JSON) */
    @PostMapping("/suggest")
    public ResponseEntity<AiRecipeEnvelopeDto> suggest(@RequestBody RecipeSuggestRequest request) {
        return ResponseEntity.ok(openAiRecipeService.suggestRecipes(request));
    }
}
