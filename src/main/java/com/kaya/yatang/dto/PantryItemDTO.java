package com.kaya.yatang.dto;

import com.kaya.yatang.db.entity.PantryItem;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PantryItemDTO {
    private Long id;
    private String name;
    private Integer quantity;
    private String unit;
    private LocalDate expirationDate;
    private LocalDate manufactureDate;
    private String memo;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long daysUntilExpiration;
    private Boolean isExpired;

    public PantryItemDTO(PantryItem item) {
        this.id = item.getId();
        this.name = item.getName();
        this.quantity = item.getQuantity();
        this.unit = item.getUnit();
        this.expirationDate = item.getExpirationDate();
        this.manufactureDate = item.getManufactureDate();
        this.memo = item.getMemo();
        this.userId = item.getUser().getId();
        this.createdAt = item.getCreatedAt();
        this.updatedAt = item.getUpdatedAt();
        this.daysUntilExpiration = item.getDaysUntilExpiration();
        this.isExpired = item.isExpired();
    }
}
