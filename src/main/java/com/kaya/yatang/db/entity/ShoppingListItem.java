package com.kaya.yatang.db.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "shopping_list_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "ingredient_name", nullable = false, length = 200)
    private String ingredientName;

    /** 예: "2", "200" — 표시용 */
    @Column(name = "quantity_note", length = 80)
    private String quantityNote;

    @Column(length = 40)
    private String unit;

    @Column(nullable = false)
    private boolean checked;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** AI 레시피 등에서 담았을 때 요리 제목 (직접 담기면 null) */
    @Column(name = "source_recipe_title", length = 200)
    private String sourceRecipeTitle;
}
