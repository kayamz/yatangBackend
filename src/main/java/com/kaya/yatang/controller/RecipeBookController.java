package com.kaya.yatang.controller;

import com.kaya.yatang.dto.recipe.SavedRecipeDetailDTO;
import com.kaya.yatang.dto.recipe.SavedRecipeSummaryDTO;
import com.kaya.yatang.dto.request.RecipeBookSaveRequest;
import com.kaya.yatang.service.SavedRecipeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recipe-book")
@RequiredArgsConstructor
public class RecipeBookController {

    private final SavedRecipeService savedRecipeService;

    @GetMapping
    public ResponseEntity<List<SavedRecipeSummaryDTO>> list(@RequestParam Long userId) {
        return ResponseEntity.ok(savedRecipeService.list(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SavedRecipeDetailDTO> get(@RequestParam Long userId, @PathVariable Long id) {
        return ResponseEntity.ok(savedRecipeService.get(userId, id));
    }

    @PostMapping
    public ResponseEntity<SavedRecipeSummaryDTO> save(
            @RequestParam Long userId, @RequestBody RecipeBookSaveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRecipeService.save(userId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@RequestParam Long userId, @PathVariable Long id) {
        savedRecipeService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
