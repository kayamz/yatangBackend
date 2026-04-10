package com.kaya.yatang.dto.recipe;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RecipeSuggestRequest {
    /** 보유 재료 (냉장·냉동·상온 합산 목록) */
    private List<RecipeIngredientLine> ingredients;
}
