package com.kaya.yatang.dto.recipe;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RecipeSuggestRequest {
    /** 보유 재료 (냉장·냉동·상온 합산 목록) */
    private List<RecipeIngredientLine> ingredients;

    /**
     * 이 브라우저에서 최근 AI가 제안한 요리 제목(선택). 같은 재료로 반복할 때 메뉴 다양화에 사용합니다.
     * 서버는 최대 30개만 반영합니다.
     */
    private List<String> recentRecipeTitles;
}
