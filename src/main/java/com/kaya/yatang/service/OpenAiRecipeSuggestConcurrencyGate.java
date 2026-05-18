package com.kaya.yatang.service;

import com.kaya.yatang.config.OpenAiProperties;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * OpenAI 레시피 추천 동시 호출 수를 제한해 서버·외부 API를 보호합니다. (단일 인스턴스 기준 in-memory 세마포어)
 */
@Component
@RequiredArgsConstructor
public class OpenAiRecipeSuggestConcurrencyGate {

    private final OpenAiProperties openAiProperties;
    private Semaphore semaphore;

    @PostConstruct
    void init() {
        int permits = Math.max(1, openAiProperties.getMaxConcurrentRecipeSuggests());
        this.semaphore = new Semaphore(permits);
    }

    public void enter() {
        long timeoutMs = Math.max(1_000L, openAiProperties.getRecipeSuggestAcquireTimeoutMs());
        try {
            if (!semaphore.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS)) {
                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE, "지금은 AI 요청이 많아 잠시 후 다시 시도해주세요.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "요청 처리가 지연되었습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    public void leave() {
        semaphore.release();
    }
}
