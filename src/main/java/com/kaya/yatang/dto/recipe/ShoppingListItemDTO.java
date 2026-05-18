package com.kaya.yatang.dto.recipe;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingListItemDTO {
    private Long id;
    private String ingredientName;
    private String quantityNote;
    private String unit;
    private String sourceRecipeTitle;
    private boolean checked;
    private LocalDateTime createdAt;
}
