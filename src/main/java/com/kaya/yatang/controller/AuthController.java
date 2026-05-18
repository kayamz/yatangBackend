package com.kaya.yatang.controller;

import com.kaya.yatang.dto.auth.AuthTokenResponse;
import com.kaya.yatang.dto.auth.RefreshTokenRequest;
import com.kaya.yatang.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final RefreshTokenService refreshTokenService;

    @PostMapping("/refresh")
    public AuthTokenResponse refresh(@RequestBody RefreshTokenRequest request) {
        try {
            return refreshTokenService.refresh(request.getRefreshToken());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    @PostMapping("/logout")
    public void logout(@RequestBody(required = false) RefreshTokenRequest request) {
        if (request != null) {
            refreshTokenService.revoke(request.getRefreshToken());
        }
    }
}
