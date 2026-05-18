package com.kaya.yatang.controller;

import com.kaya.yatang.dto.recipe.SavedRecipeDetailDTO;
import com.kaya.yatang.dto.recipe.SavedRecipeSummaryDTO;
import com.kaya.yatang.dto.request.RecipeBookSaveRequest;
import com.kaya.yatang.security.CurrentUser;
import com.kaya.yatang.service.SavedRecipeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recipe-book")
@RequiredArgsConstructor
public class RecipeBookController {

    private final SavedRecipeService savedRecipeService;
    private final CurrentUser currentUser;

    @GetMapping
    public ResponseEntity<List<SavedRecipeSummaryDTO>> list(Authentication authentication) {
        return ResponseEntity.ok(savedRecipeService.list(currentUser.id(authentication)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SavedRecipeDetailDTO> get(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(savedRecipeService.get(currentUser.id(authentication), id));
    }

    @PostMapping
    public ResponseEntity<SavedRecipeSummaryDTO> save(
            Authentication authentication, @RequestBody RecipeBookSaveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRecipeService.save(currentUser.id(authentication), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        savedRecipeService.delete(currentUser.id(authentication), id);
        return ResponseEntity.noContent().build();
    }
}
