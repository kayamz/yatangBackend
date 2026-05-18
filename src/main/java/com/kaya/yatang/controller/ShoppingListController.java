package com.kaya.yatang.controller;

import com.kaya.yatang.dto.recipe.ShoppingListItemDTO;
import com.kaya.yatang.dto.request.ShoppingListAddLineRequest;
import com.kaya.yatang.security.CurrentUser;
import com.kaya.yatang.service.ShoppingListService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shopping-list")
@RequiredArgsConstructor
public class ShoppingListController {

    private final ShoppingListService shoppingListService;
    private final CurrentUser currentUser;

    @GetMapping
    public ResponseEntity<List<ShoppingListItemDTO>> list(Authentication authentication) {
        return ResponseEntity.ok(shoppingListService.list(currentUser.id(authentication)));
    }

    @PostMapping
    public ResponseEntity<ShoppingListItemDTO> add(
            Authentication authentication, @RequestBody ShoppingListAddLineRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shoppingListService.add(currentUser.id(authentication), request));
    }

    @PostMapping("/batch")
    public ResponseEntity<List<ShoppingListItemDTO>> addBatch(
            Authentication authentication, @RequestBody List<ShoppingListAddLineRequest> lines) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shoppingListService.addBatch(currentUser.id(authentication), lines));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ShoppingListItemDTO> toggle(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(shoppingListService.toggleChecked(currentUser.id(authentication), id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        shoppingListService.delete(currentUser.id(authentication), id);
        return ResponseEntity.noContent().build();
    }
}
