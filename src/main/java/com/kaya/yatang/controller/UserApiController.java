package com.kaya.yatang.controller;

import com.kaya.yatang.db.entity.User;
import com.kaya.yatang.db.repository.UserRepository;
import com.kaya.yatang.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UserApiController {

    @Autowired
    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    // 회원가입
//    @PostMapping("/register")
//    public UUID register(@RequestBody Map<String, String> user) {
//        return memberRepository.save(Member.builder()
//                .email(user.get("email"))
//                .password(passwordEncoder.encode(user.get("password")))
//                .nickname(user.get("nickname"))
//                .phone(user.get("phone"))
//                .role(Role.ROLE_MEMBER)
//                .build()).getId();
//    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> user) {
        try {
            User userEntity = userRepository
                    .findByUsername(user.get("username"))
                    .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 아이디입니다."));

            if (!passwordEncoder.matches(user.get("password"), userEntity.getPassword())) {
                throw new IllegalArgumentException("아이디 또는 비밀번호가 맞지 않습니다.");
            }

            return ResponseEntity.ok(refreshTokenService.issueTokens(userEntity));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", ex.getMessage()));
        }
    }
}
