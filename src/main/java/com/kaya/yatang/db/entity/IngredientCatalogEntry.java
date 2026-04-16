package com.kaya.yatang.db.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ingredient_catalog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredientCatalogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "default_unit", nullable = false, length = 20)
    private String defaultUnit;

    /** 시스템 공용 분류 (null이면 기타·구버전 데이터) */
    @Column(length = 80)
    private String category;

    /**
     * 시스템 카탈로그 전용: public 폴더 기준 아이콘 파일명(영문). 예: onion.png 또는 icons/onion.png
     * 관리용이며 UI에 노출하지 않고, 프론트에서 {@code /ingredient-icons/} 등과 조합합니다.
     */
    @Column(name = "icon_image_file", length = 255)
    private String iconImageFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
