package com.kaya.yatang.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "yatang.openai")
public class OpenAiProperties {

    /** 환경변수 OPENAI_API_KEY 권장 */
    private String apiKey = "";

    /** 기본: gpt-4o-mini (가성비·한국어 요리에 무난) */
    private String model = "gpt-4o-mini";

    private String baseUrl = "https://api.openai.com/v1";
}
