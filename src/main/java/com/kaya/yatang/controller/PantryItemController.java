package com.kaya.yatang.controller;

import com.kaya.yatang.dto.PantryItemDTO;
import com.kaya.yatang.dto.request.ItemRequest;
import com.kaya.yatang.service.PantryItemService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pantry/items")
@RequiredArgsConstructor
public class PantryItemController {

    private final PantryItemService pantryItemService;

    @PostMapping
    public ResponseEntity<PantryItemDTO> createItem(
            @RequestParam Long userId,
            @RequestBody ItemRequest request) {
        PantryItemDTO item = pantryItemService.createItem(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @GetMapping
    public ResponseEntity<List<PantryItemDTO>> getItems(@RequestParam Long userId) {
        return ResponseEntity.ok(pantryItemService.getItems(userId));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<PantryItemDTO> getItem(
            @PathVariable Long itemId,
            @RequestParam Long userId) {
        return ResponseEntity.ok(pantryItemService.getItemById(itemId, userId));
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<PantryItemDTO> updateItem(
            @PathVariable Long itemId,
            @RequestParam Long userId,
            @RequestBody ItemRequest request) {
        return ResponseEntity.ok(pantryItemService.updateItem(itemId, userId, request));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Map<String, String>> deleteItem(
            @PathVariable Long itemId,
            @RequestParam Long userId) {
        pantryItemService.deleteItem(itemId, userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "아이템이 삭제되었습니다.");
        return ResponseEntity.ok(response);
    }
}
