package com.kaya.yatang.dto.recipe;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiSuggestQuotaDto {
    /** 오늘(한국시간) 이미 사용한 횟수 */
    private int usedToday;
    /** 일일 한도 */
    private int limitToday;
    /** 남은 횟수 (추가 호출 가능 횟수) */
    private int remainingToday;
}
