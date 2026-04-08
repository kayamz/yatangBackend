package com.kaya.yatang.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class IngredientCatalogCreateRequest {
    private String name;
    private String defaultUnit;
}
