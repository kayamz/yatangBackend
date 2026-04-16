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
    /** 시스템 항목만: 아이콘 파일명(영문 등). 관리·매핑용, 사용자 화면에 직접 노출하지 않음 */
    private String iconImageFile;
    /** 로그인 사용자가 해당 재료명에 등록한 이미지 공개 URL (없으면 null) */
    private String userImageUrl;

    public IngredientCatalogDTO(IngredientCatalogEntry e) {
        this.id = e.getId();
        this.name = e.getName();
        this.defaultUnit = e.getDefaultUnit();
        this.category = e.getCategory();
        this.custom = e.getUser() != null;
        this.iconImageFile = e.getIconImageFile();
    }
}
