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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
