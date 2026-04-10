package com.kaya.yatang.dto.recipe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiRecipeDto {
    private String title;
    private Integer cookMinutes;
    private Integer servings;
    private List<RecipeIngredientAmountDto> ingredients = new ArrayList<>();
    private List<String> steps = new ArrayList<>();
    private List<RecipeIngredientAmountDto> missingIngredients = new ArrayList<>();
    /** 재고에서 활용하는 재료 설명 (선택) */
    private List<String> usesFromInventory = new ArrayList<>();
}
