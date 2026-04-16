package com.kaya.yatang.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "user_ingredient_images",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "ingredient_name"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserIngredientImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** trim 된 재료 이름 (카탈로그·냉장고 아이템 이름과 매칭) */
    @Column(name = "ingredient_name", nullable = false, length = 120)
    private String ingredientName;

    /** 공개 조회용 (이미지 URL에 사용) */
    @Column(name = "public_id", nullable = false, unique = true, length = 36)
    private String publicId;

    @Column(name = "content_type", nullable = false, length = 80)
    private String contentType;

    /** 확장자 (예: png, jpg) — 저장 파일명에 사용 */
    @Column(name = "file_extension", nullable = false, length = 10)
    private String fileExtension;
}
