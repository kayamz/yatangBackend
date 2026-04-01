package com.kaya.yatang.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 추후 OAuth 연동을 위한 임시 라우트.
 * 프론트엔드에서는 해당 엔드포인트가 존재하는지만 확인하고
 * 실제 카카오/구글 인증 연동은 추후 진행한다.
 */

// Todo : 간편 로그인 연동 성공시키기, DB에 잘 기록되는지 test
@RestController
@RequestMapping("/api/oauth2")
public class OAuthController {

    private static final Map<String, String> SUPPORTED_PROVIDERS = Map.of(
        "kakao", "카카오 간편 로그인",
        "google", "Google 간편 로그인"
    );

    @GetMapping("/authorization/{provider}")
    public ResponseEntity<Map<String, Object>> authorize(@PathVariable String provider) {
        Map<String, Object> response = new HashMap<>();

        if (!SUPPORTED_PROVIDERS.containsKey(provider)) {
            response.put("provider", provider);
            response.put("message", "지원되지 않는 소셜 로그인 공급자입니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        response.put("provider", provider);
        response.put("message", SUPPORTED_PROVIDERS.get(provider) + " 연동을 준비 중입니다.");
        response.put("status", "pending");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/callback/{provider}")
    public ResponseEntity<Map<String, Object>> callback(@PathVariable String provider) {
        Map<String, Object> response = new HashMap<>();
        response.put("provider", provider);
        response.put("message", "OAuth 콜백이 도착했습니다. 정식 연동 시 토큰 발급 로직을 구현하세요.");
        response.put("status", "pending");
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(response);
    }
}
