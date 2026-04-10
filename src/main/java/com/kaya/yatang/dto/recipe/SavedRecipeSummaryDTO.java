package com.kaya.yatang.dto.recipe;

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
public class SavedRecipeSummaryDTO {
    private Long id;
    private String title;
    private Integer servings;
    private Integer cookMinutes;
    private LocalDateTime createdAt;
}
