package com.kaya.yatang.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ShoppingListAddLineRequest {
    private String ingredientName;
    private String quantityNote;
    private String unit;
    /** 부족 재료 일괄 담기 시 어떤 레시피에서 왔는지 (표시용) */
    private String sourceRecipeTitle;
}
