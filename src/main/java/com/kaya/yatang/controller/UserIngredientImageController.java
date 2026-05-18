package com.kaya.yatang.controller;

import com.kaya.yatang.security.CurrentUser;
import com.kaya.yatang.service.UserIngredientImageService;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ingredient-images")
@RequiredArgsConstructor
public class UserIngredientImageController {

    private final UserIngredientImageService userIngredientImageService;
    private final CurrentUser currentUser;

    /** 재료명(소문자 키) → 공개 이미지 URL — 냉장고 그리드에서 매칭용 */
    @GetMapping("/map")
    public Map<String, String> map(Authentication authentication) {
        Map<String, String> ids = userIngredientImageService.getPublicIdByIngredientNameLower(currentUser.id(authentication));
        Map<String, String> urls = new HashMap<>();
        for (Map.Entry<String, String> e : ids.entrySet()) {
            urls.put(e.getKey(), "/api/public/ingredient-images/" + e.getValue());
        }
        return urls;
    }

    @PostMapping
    public ResponseEntity<Void> upload(
            Authentication authentication,
            @RequestParam String ingredientName,
            @RequestParam("file") MultipartFile file)
            throws Exception {
        userIngredientImageService.upload(currentUser.id(authentication), ingredientName, file);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(Authentication authentication, @RequestParam String ingredientName) {
        userIngredientImageService.delete(currentUser.id(authentication), ingredientName);
        return ResponseEntity.noContent().build();
    }
}
