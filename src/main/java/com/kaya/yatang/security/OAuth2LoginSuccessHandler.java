package com.kaya.yatang.security;

import com.kaya.yatang.db.entity.User;
import com.kaya.yatang.dto.auth.AuthTokenResponse;
import com.kaya.yatang.service.OAuthAccountService;
import com.kaya.yatang.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final OAuthAccountService oAuthAccountService;
    private final RefreshTokenService refreshTokenService;

    @Value("${yatang.oauth2.frontend-redirect-url:http://localhost:3000/oauth/callback}")
    private String frontendRedirectUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        if (!(authentication instanceof OAuth2AuthenticationToken token)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "OAuth2AuthenticationToken 아님");
            return;
        }
        OAuth2User oauth2User = token.getPrincipal();
        String registrationId = token.getAuthorizedClientRegistrationId();

        User user = oAuthAccountService.findOrCreateFromOAuth(registrationId, oauth2User);
        AuthTokenResponse tokens = refreshTokenService.issueTokens(user);

        String base = frontendRedirectUrl.split("\\?")[0];
        String target = base
                + "?accessToken=" + URLEncoder.encode(tokens.getAccessToken(), StandardCharsets.UTF_8)
                + "&refreshToken=" + URLEncoder.encode(tokens.getRefreshToken(), StandardCharsets.UTF_8);
        getRedirectStrategy().sendRedirect(request, response, target);
    }
}
