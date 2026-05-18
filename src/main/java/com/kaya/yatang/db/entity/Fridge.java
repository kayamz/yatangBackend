package com.kaya.yatang.db.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fridge")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fridge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(name = "create_date", nullable = false)
    private LocalDate createDate;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "isMain")
    private Boolean isMain;

    @Column(name = "deleted")
    private Boolean deleted;

    // 🔹 User(소유자) 관계 (ManyToOne)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 🔹 냉장실 아이템들 (OneToMany)
    @Builder.Default
    @OneToMany(mappedBy = "fridge", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FridgeItem> fridgeItems = new ArrayList<>();

    // 🔹 냉동실 아이템들 (OneToMany)
    @Builder.Default
    @OneToMany(mappedBy = "fridge", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FreezerItem> freezerItems = new ArrayList<>();

    // 🔹 생성일 자동 설정
    @PrePersist
    public void prePersist() {
        this.createDate = LocalDate.now();
    }

    // 🔹 수정일 자동 업데이트
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 🔹 편의 메서드 - 냉장실 아이템 추가
    public void addFridgeItem(FridgeItem item) {
        fridgeItems.add(item);
        item.setFridge(this);
    }

    // 🔹 편의 메서드 - 냉동실 아이템 추가
    public void addFreezerItem(FreezerItem item) {
        freezerItems.add(item);
        item.setFridge(this);
    }

    // 🔹 정적 팩토리 메서드 - 일반 냉장고 생성
    public static Fridge createFridge(User user, Boolean isMain, String name, String description) {
        return Fridge.builder()
            .name(name)
            .description(description)
            .isMain(isMain)
            .user(user)
            .fridgeItems(new ArrayList<>())
            .freezerItems(new ArrayList<>())
            .build();
    }

    // 🔹 정적 팩토리 메서드 - 메인 냉장고 생성
    public static Fridge createMainFridge(User user) {
        return createFridge(user, true, "메인 냉장고", null);
    }
}