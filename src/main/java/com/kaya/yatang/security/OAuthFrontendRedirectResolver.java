package com.kaya.yatang.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 웹 vs 앱(딥링크) OAuth 완료 리다이렉트 URL.
 * 앱 여부는 세션 + 쿠키(yatang_oauth_client)로 판별합니다.
 */
@Component
public class OAuthFrontendRedirectResolver {

    public static final String SESSION_CLIENT_KEY = "yatang.oauth.client";
    public static final String COOKIE_CLIENT_KEY = "yatang_oauth_client";
    public static final String CLIENT_APP = "app";

    @Value("${yatang.oauth2.frontend-redirect-url:http://localhost:3000/oauth/callback}")
    private String webRedirectUrl;

    @Value("${yatang.oauth2.app-redirect-url:com.kaya.yatang://oauth/callback}")
    private String appRedirectUrl;

    public void markClient(HttpServletRequest request, HttpServletResponse response, String client) {
        boolean app = CLIENT_APP.equalsIgnoreCase(client);
        String value = app ? CLIENT_APP : "web";
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_CLIENT_KEY, value);

        response.addHeader(
                "Set-Cookie",
                COOKIE_CLIENT_KEY + "=" + value + "; Path=/; Max-Age=600; HttpOnly; SameSite=Lax");
    }

    public boolean isAppClient(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && CLIENT_APP.equals(session.getAttribute(SESSION_CLIENT_KEY))) {
            return true;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (COOKIE_CLIENT_KEY.equals(c.getName()) && CLIENT_APP.equals(c.getValue())) {
                    return true;
                }
            }
        }
        return false;
    }

    public String baseRedirectUrl(HttpServletRequest request) {
        // client=app 으로 시작한 경우(외부 브라우저 복귀)만 딥링크
        if (isAppClient(request)) {
            return appRedirectUrl.split("\\?")[0];
        }
        return webRedirectUrl.split("\\?")[0];
    }

    public String successUrl(HttpServletRequest request, String accessToken, String refreshToken) {
        String base = baseRedirectUrl(request);
        return base
                + "?accessToken=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8)
                + "&refreshToken=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8);
    }

    public String errorUrl(HttpServletRequest request, String message) {
        String base = baseRedirectUrl(request);
        String msg = message == null || message.isBlank() ? "oauth_failed" : message;
        return base + "?error=" + URLEncoder.encode(msg, StandardCharsets.UTF_8);
    }
}
