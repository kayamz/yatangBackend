package com.kaya.yatang.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "yatang.ingredient-images")
public class IngredientImageProperties {

    /** 업로드 파일 저장 디렉터리 (절대 또는 상대 경로) */
    private String storageDir = "uploads/user-ingredient-images";
}
