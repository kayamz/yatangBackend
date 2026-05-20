package com.kaya.yatang.controller;

import com.kaya.yatang.dto.UserDTO;
import com.kaya.yatang.dto.request.GuestImportRequest;
import com.kaya.yatang.dto.request.NicknameUpdateRequest;
import com.kaya.yatang.dto.request.PasswordUpdateRequest;
import com.kaya.yatang.dto.request.SignupRequest;
import com.kaya.yatang.dto.response.SignupResponse;
import com.kaya.yatang.security.CurrentUser;
import com.kaya.yatang.service.RefreshTokenService;
import com.kaya.yatang.service.UserService;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CurrentUser currentUser;
    private final RefreshTokenService refreshTokenService;

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
            Authentication authentication,
            @RequestBody NicknameUpdateRequest request) {

        UserDTO updatedUser = userService.updateNickname(currentUser.id(authentication), request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * 내 정보 조회 (DTO 반환)
     */
    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserDTO> getUserProfile(Authentication authentication) {
        UserDTO userProfile = userService.getUserProfile(currentUser.id(authentication));
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
            Authentication authentication,
            @RequestBody PasswordUpdateRequest request) {

        Long userId = currentUser.id(authentication);
        userService.updatePassword(userId, request.getCurrentPassword(), request.getNewPassword());
        refreshTokenService.revokeAllForUser(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * 게스트(로컬) 기록을 로그인 계정으로 병합(import)
     */
    @PostMapping("/{userId}/guest-import")
    public ResponseEntity<Map<String, Object>> importGuestData(
            Authentication authentication,
            @RequestBody GuestImportRequest request) {
        Map<String, Object> result = userService.importGuestData(currentUser.id(authentication), request);
        return ResponseEntity.ok(result);
    }

    /**
     * 회원 탈퇴 (본인 계정·연관 데이터 삭제)
     */
    @DeleteMapping("/{userId}/account")
    public ResponseEntity<Void> deleteAccount(Authentication authentication) {
        userService.deleteAccount(currentUser.id(authentication));
        return ResponseEntity.noContent().build();
    }
}