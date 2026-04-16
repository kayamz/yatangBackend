package com.kaya.yatang.controller;

import com.kaya.yatang.dto.IngredientCatalogDTO;
import com.kaya.yatang.dto.request.IngredientCatalogCreateRequest;
import com.kaya.yatang.service.IngredientCatalogService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ingredients-catalog")
@RequiredArgsConstructor
public class IngredientCatalogController {

    private final IngredientCatalogService ingredientCatalogService;

    @GetMapping
    public ResponseEntity<List<IngredientCatalogDTO>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(ingredientCatalogService.listCatalog(q, userId, category));
    }

    /** 시스템 재료명(소문자) → 아이콘 파일명(영문). 냉장고 그리드·캐시용. */
    @GetMapping("/icon-map")
    public ResponseEntity<Map<String, String>> iconMap() {
        return ResponseEntity.ok(ingredientCatalogService.getSystemIconFileByNameLower());
    }

    @PostMapping
    public ResponseEntity<IngredientCatalogDTO> addCustom(
            @RequestParam Long userId,
            @RequestBody IngredientCatalogCreateRequest request) {
        IngredientCatalogDTO dto = ingredientCatalogService.addCustom(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustom(@RequestParam Long userId, @PathVariable Long id) {
        ingredientCatalogService.deleteCustom(userId, id);
        return ResponseEntity.noContent().build();
    }
}
