package com.kaya.yatang.dto;

import com.kaya.yatang.db.entity.FreezerItem;
import com.kaya.yatang.db.entity.FridgeItem;
import com.kaya.yatang.db.entity.PantryItem;
import java.time.LocalDate;
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
public class AggregatedItemDTO {
    private Long itemId;
    private Long fridgeId;
    private String fridgeName;
    /** "냉장실" or "냉동실" */
    private String storageType;
    private String name;
    private Integer quantity;
    private String unit;
    private LocalDate expirationDate;
    private LocalDate manufactureDate;
    private String memo;
    private Long daysUntilExpiration;
    /** 등록일시(생성일) */
    private LocalDateTime createdAt;

    public static AggregatedItemDTO fromFridgeItem(FridgeItem item, String fridgeName) {
        return AggregatedItemDTO.builder()
                .itemId(item.getId())
                .fridgeId(item.getFridge().getId())
                .fridgeName(fridgeName)
                .storageType("냉장실")
                .name(item.getName())
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .expirationDate(item.getExpirationDate())
                .manufactureDate(item.getManufactureDate())
                .memo(item.getMemo())
                .daysUntilExpiration(item.getDaysUntilExpiration())
                .createdAt(item.getCreatedAt())
                .build();
    }

    public static AggregatedItemDTO fromFreezerItem(FreezerItem item, String fridgeName) {
        return AggregatedItemDTO.builder()
                .itemId(item.getId())
                .fridgeId(item.getFridge().getId())
                .fridgeName(fridgeName)
                .storageType("냉동실")
                .name(item.getName())
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .expirationDate(item.getExpirationDate())
                .manufactureDate(item.getManufactureDate())
                .memo(item.getMemo())
                .daysUntilExpiration(item.getDaysUntilExpiration())
                .createdAt(item.getCreatedAt())
                .build();
    }

    public static AggregatedItemDTO fromPantryItem(PantryItem item) {
        return AggregatedItemDTO.builder()
                .itemId(item.getId())
                .fridgeId(null)
                .fridgeName("상온보관")
                .storageType("상온보관")
                .name(item.getName())
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .expirationDate(item.getExpirationDate())
                .manufactureDate(item.getManufactureDate())
                .memo(item.getMemo())
                .daysUntilExpiration(item.getDaysUntilExpiration())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
