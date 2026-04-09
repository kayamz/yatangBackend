package com.kaya.yatang.dto;

import com.kaya.yatang.db.entity.IngredientCatalogEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IngredientCatalogDTO {
    private Long id;
    private String name;
    private String defaultUnit;
    private String category;
    private boolean custom;

    public IngredientCatalogDTO(IngredientCatalogEntry e) {
        this.id = e.getId();
        this.name = e.getName();
        this.defaultUnit = e.getDefaultUnit();
        this.category = e.getCategory();
        this.custom = e.getUser() != null;
    }
}
