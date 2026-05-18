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

    /**
     * 동시에 OpenAI 레시피 추천 HTTP 호출을 진행할 수 있는 최대 개수(인스턴스당). 초과 시 대기 후 타임아웃이면 503.
     */
    private int maxConcurrentRecipeSuggests = 3;

    /** 세마포어 대기 최대 시간(ms). 이 시간 안에 슬롯을 못 잡으면 503 SERVICE_UNAVAILABLE */
    private long recipeSuggestAcquireTimeoutMs = 90_000L;
}
