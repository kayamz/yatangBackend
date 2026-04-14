package com.kaya.yatang.dto.recipe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecipeIngredientAmountDto {
    private String name;
    /** 예: "200g", "1/2큰술", "3개" */
    private String amount;
    private String note;
    /**
     * 대체 가능한 재료 (예: 굴소스 → "치킨스톡"). 없거나 불필요하면 빈 문자열.
     */
    private String substitute;
}
