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
}
