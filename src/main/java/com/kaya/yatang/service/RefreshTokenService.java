package com.kaya.yatang.service;

import com.kaya.yatang.db.entity.RefreshToken;
import com.kaya.yatang.db.entity.User;
import com.kaya.yatang.db.repository.RefreshTokenRepository;
import com.kaya.yatang.db.repository.UserRepository;
import com.kaya.yatang.dto.auth.AuthTokenResponse;
import com.kaya.yatang.security.JwtTokenProvider;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${jwt.refresh-expiration-ms:2592000000}")
    private long refreshTokenValidTime;

    @Transactional
    public AuthTokenResponse issueTokens(User user) {
        String accessToken = jwtTokenProvider.createToken(user.getId(), user.getUsername());
        String refreshToken = createOpaqueToken();

        RefreshToken entity = new RefreshToken();
        entity.setUser(user);
        entity.setTokenHash(hash(refreshToken));
        entity.setExpiresAt(LocalDateTime.now().plus(Duration.ofMillis(refreshTokenValidTime)));
        refreshTokenRepository.save(entity);

        return new AuthTokenResponse(accessToken, refreshToken, "Bearer", jwtTokenProvider.getTokenValidTime());
    }

    @Transactional
    public AuthTokenResponse refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("refreshToken이 필요합니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        RefreshToken current = refreshTokenRepository.findByTokenHash(hash(refreshToken))
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 refresh token입니다."));

        if (!current.isActive(now)) {
            throw new IllegalArgumentException("만료되었거나 폐기된 refresh token입니다.");
        }

        current.setRevokedAt(now);
        Long userId = current.getUser().getId();
        if (userId == null) {
            throw new IllegalArgumentException("refresh token의 사용자 정보가 올바르지 않습니다.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        return issueTokens(user);
    }

    @Transactional
    public void revoke(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        refreshTokenRepository.revokeByTokenHash(hash(refreshToken));
    }

    @Transactional
    public void revokeAllForUser(Long userId) {
        refreshTokenRepository.revokeAllActiveByUserId(userId);
    }

    private String createOpaqueToken() {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", e);
        }
    }
}
