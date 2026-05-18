package com.kaya.yatang.controller;

import com.kaya.yatang.dto.PantryItemDTO;
import com.kaya.yatang.dto.request.ItemRequest;
import com.kaya.yatang.security.CurrentUser;
import com.kaya.yatang.service.PantryItemService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pantry/items")
@RequiredArgsConstructor
public class PantryItemController {

    private final PantryItemService pantryItemService;
    private final CurrentUser currentUser;

    @PostMapping
    public ResponseEntity<PantryItemDTO> createItem(
            Authentication authentication,
            @RequestBody ItemRequest request) {
        PantryItemDTO item = pantryItemService.createItem(currentUser.id(authentication), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @GetMapping
    public ResponseEntity<List<PantryItemDTO>> getItems(Authentication authentication) {
        return ResponseEntity.ok(pantryItemService.getItems(currentUser.id(authentication)));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<PantryItemDTO> getItem(
            @PathVariable Long itemId,
            Authentication authentication) {
        return ResponseEntity.ok(pantryItemService.getItemById(itemId, currentUser.id(authentication)));
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<PantryItemDTO> updateItem(
            @PathVariable Long itemId,
            Authentication authentication,
            @RequestBody ItemRequest request) {
        return ResponseEntity.ok(pantryItemService.updateItem(itemId, currentUser.id(authentication), request));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Map<String, String>> deleteItem(
            @PathVariable Long itemId,
            Authentication authentication) {
        pantryItemService.deleteItem(itemId, currentUser.id(authentication));
        Map<String, String> response = new HashMap<>();
        response.put("message", "아이템이 삭제되었습니다.");
        return ResponseEntity.ok(response);
    }
}
