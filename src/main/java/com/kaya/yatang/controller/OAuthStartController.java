package com.kaya.yatang.controller;

import com.kaya.yatang.security.OAuthFrontendRedirectResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 앱(Custom Tab)에서 OAuth를 시작할 때 사용.
 * 같은 브라우저 세션에 client=app 을 남긴 뒤 Spring OAuth2 인가 URL로 보냅니다.
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class OAuthStartController {

    private final OAuthFrontendRedirectResolver redirectResolver;

    @GetMapping("/oauth-start")
    public void start(
            @RequestParam String provider,
            @RequestParam(defaultValue = "web") String client,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {
        String id = provider == null ? "" : provider.trim().toLowerCase();
        if (!"google".equals(id) && !"kakao".equals(id)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "provider는 google 또는 kakao");
            return;
        }
        redirectResolver.markClient(request, response, client);
        response.sendRedirect(request.getContextPath() + "/oauth2/authorization/" + id);
    }
}
