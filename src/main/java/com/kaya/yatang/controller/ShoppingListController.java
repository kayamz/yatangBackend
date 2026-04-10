package com.kaya.yatang.controller;

import com.kaya.yatang.dto.recipe.ShoppingListItemDTO;
import com.kaya.yatang.dto.request.ShoppingListAddLineRequest;
import com.kaya.yatang.service.ShoppingListService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shopping-list")
@RequiredArgsConstructor
public class ShoppingListController {

    private final ShoppingListService shoppingListService;

    @GetMapping
    public ResponseEntity<List<ShoppingListItemDTO>> list(@RequestParam Long userId) {
        return ResponseEntity.ok(shoppingListService.list(userId));
    }

    @PostMapping
    public ResponseEntity<ShoppingListItemDTO> add(
            @RequestParam Long userId, @RequestBody ShoppingListAddLineRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shoppingListService.add(userId, request));
    }

    @PostMapping("/batch")
    public ResponseEntity<List<ShoppingListItemDTO>> addBatch(
            @RequestParam Long userId, @RequestBody List<ShoppingListAddLineRequest> lines) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shoppingListService.addBatch(userId, lines));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ShoppingListItemDTO> toggle(@RequestParam Long userId, @PathVariable Long id) {
        return ResponseEntity.ok(shoppingListService.toggleChecked(userId, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@RequestParam Long userId, @PathVariable Long id) {
        shoppingListService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
