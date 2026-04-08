package com.kaya.yatang.controller;

import com.kaya.yatang.dto.IngredientCatalogDTO;
import com.kaya.yatang.dto.request.IngredientCatalogCreateRequest;
import com.kaya.yatang.service.IngredientCatalogService;
import java.util.List;
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
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(ingredientCatalogService.listCatalog(q, userId));
    }

    @PostMapping
    public ResponseEntity<IngredientCatalogDTO> addCustom(
            @RequestParam Long userId,
            @RequestBody IngredientCatalogCreateRequest request) {
        IngredientCatalogDTO dto = ingredientCatalogService.addCustom(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
}
