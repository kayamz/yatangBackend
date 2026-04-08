package com.kaya.yatang.dto;

import com.kaya.yatang.db.entity.FreezerItem;
import com.kaya.yatang.db.entity.FridgeItem;
import java.time.LocalDate;
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
                .build();
    }
}
