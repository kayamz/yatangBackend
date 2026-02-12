package com.kaya.yatang.controller;

import com.kaya.yatang.dto.UserDTO;
import com.kaya.yatang.dto.request.NicknameUpdateRequest;
import com.kaya.yatang.dto.request.PasswordUpdateRequest;
import com.kaya.yatang.dto.request.SignupRequest;
import com.kaya.yatang.dto.response.SignupResponse;
import com.kaya.yatang.service.UserService;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@RequestBody SignupRequest request) {
        SignupResponse response = userService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 닉네임 업데이트
     */
    @PatchMapping("/{userId}/nickname")
    public ResponseEntity<UserDTO> updateNickname(
            @PathVariable Long userId,
            @RequestBody NicknameUpdateRequest request) {

        UserDTO updatedUser = userService.updateNickname(userId, request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * 내 정보 조회 (DTO 반환)
     */
    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserDTO> getUserProfile(@PathVariable Long userId) {
        UserDTO userProfile = userService.getUserProfile(userId);
        return ResponseEntity.ok(userProfile);
    }

    /**
     * 닉네임 중복 확인
     */
    @GetMapping("/check-nickname")
    public ResponseEntity<Map<String, Object>> checkNickname(@RequestParam String nickname) {
        boolean isAvailable = !userService.isNicknameExists(nickname);

        Map<String, Object> response = new HashMap<>();
        response.put("available", isAvailable);

        return ResponseEntity.ok(response);
    }

    /**
     * 이메일 중복 확인
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam String email) {
        boolean isAvailable = !userService.isEmailExists(email);

        Map<String, Object> response = new HashMap<>();
        response.put("available", isAvailable);

        return ResponseEntity.ok(response);
    }

    /**
     * 사용자명 중복 확인
     */
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Object>> checkUsername(@RequestParam String username) {
        boolean isAvailable = !userService.isUsernameExists(username);

        Map<String, Object> response = new HashMap<>();
        response.put("available", isAvailable);

        return ResponseEntity.ok(response);
    }

    /**
     * 비밀번호 변경
     */
    @PatchMapping("/{userId}/password")
    public ResponseEntity<Void> updatePassword(
            @PathVariable Long userId,
            @RequestBody PasswordUpdateRequest request) {

        userService.updatePassword(userId, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }
}