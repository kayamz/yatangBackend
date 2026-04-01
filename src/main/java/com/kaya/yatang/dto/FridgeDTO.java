package com.kaya.yatang.dto;

import com.kaya.yatang.db.entity.Fridge;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FridgeDTO {
    private Long id;
    private String name;
    private String description;
    private LocalDate createDate;
    private LocalDateTime updatedAt;
    private Boolean isMain;
    private Boolean deleted;

    // 소유자 정보
    private Long userId;
    private String userNickname;

    // 아이템 통계
    private int fridgeItemCount; // 냉장실 아이템 개수
    private int freezerItemCount; // 냉동실 아이템 개수
    private int totalItemCount; // 전체 아이템 개수
    private int expiringSoonCount; // 유통기한 임박 아이템 개수 (3일 이내)

    // Entity -> DTO 변환 생성자
    public FridgeDTO(Fridge fridge) {
        this.id = fridge.getId();
        this.name = fridge.getName();
        this.description = fridge.getDescription();
        this.createDate = fridge.getCreateDate();
        this.updatedAt = fridge.getUpdatedAt();

        // 사용자 정보
        if (fridge.getUser() != null) {
            this.userId = fridge.getUser().getId();
            this.userNickname = fridge.getUser().getNickname();
        }

//        // 아이템 통계 계산
//        if (fridge.getFridgeItems() != null) {
//            this.fridgeItemCount = fridge.getFridgeItems().size();
//            this.expiringSoonCount += (int) fridge.getFridgeItems().stream()
//                .filter(item -> item.isExpiringSoon(3))
//                .count();
//        }
//
//        if (fridge.getFreezerItems() != null) {
//            this.freezerItemCount = fridge.getFreezerItems().size();
//        }

        this.totalItemCount = this.fridgeItemCount + this.freezerItemCount;
    }

    // 간단한 정보만 포함하는 생성자 (목록 조회용)
    public FridgeDTO(Long id, String name, String description, LocalDate createDate, Long userId, String userNickname) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createDate = createDate;
        this.userId = userId;
        this.userNickname = userNickname;
    }
}