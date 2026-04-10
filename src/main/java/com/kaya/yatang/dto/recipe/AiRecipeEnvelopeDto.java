package com.kaya.yatang.dto.recipe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiRecipeEnvelopeDto {
    private List<AiRecipeDto> recipes = new ArrayList<>();
}
