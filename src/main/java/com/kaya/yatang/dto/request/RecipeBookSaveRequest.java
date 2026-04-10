package com.kaya.yatang.dto.request;

import com.kaya.yatang.dto.recipe.AiRecipeDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RecipeBookSaveRequest {
    private AiRecipeDto recipe;
}
