package com.kaya.yatang.controller;

import com.kaya.yatang.dto.FridgeItemDTO;
import com.kaya.yatang.dto.request.ItemRequest;
import com.kaya.yatang.security.CurrentUser;
import com.kaya.yatang.service.FridgeItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fridges/{fridgeId}/items")
@RequiredArgsConstructor
public class FridgeItemController {

    private final FridgeItemService fridgeItemService;
    private final CurrentUser currentUser;

    /**
     * 냉장실 아이템 추가
     */
    @PostMapping
    public ResponseEntity<FridgeItemDTO> createItem(
        @PathVariable Long fridgeId,
        Authentication authentication,
        @RequestBody ItemRequest request) {

        FridgeItemDTO item = fridgeItemService.createItem(fridgeId, currentUser.id(authentication), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    /**
     * 냉장실 아이템 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<FridgeItemDTO>> getItems(
        @PathVariable Long fridgeId,
        Authentication authentication) {

        List<FridgeItemDTO> items = fridgeItemService.getItemsByFridge(fridgeId, currentUser.id(authentication));
        return ResponseEntity.ok(items);
    }

    /**
     * 냉장실 아이템 상세 조회
     */
    @GetMapping("/{itemId}")
    public ResponseEntity<FridgeItemDTO> getItem(
        @PathVariable Long fridgeId,
        @PathVariable Long itemId,
        Authentication authentication) {

        FridgeItemDTO item = fridgeItemService.getItemById(itemId, currentUser.id(authentication));
        return ResponseEntity.ok(item);
    }

    /**
     * 냉장실 아이템 수정
     */
    @PatchMapping("/{itemId}")
    public ResponseEntity<FridgeItemDTO> updateItem(
        @PathVariable Long fridgeId,
        @PathVariable Long itemId,
        Authentication authentication,
        @RequestBody ItemRequest request) {

        FridgeItemDTO item = fridgeItemService.updateItem(itemId, currentUser.id(authentication), request);
        return ResponseEntity.ok(item);
    }

    /**
     * 냉장실 아이템 삭제
     */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Map<String, String>> deleteItem(
        @PathVariable Long fridgeId,
        @PathVariable Long itemId,
        Authentication authentication) {

        fridgeItemService.deleteItem(itemId, currentUser.id(authentication));

        Map<String, String> response = new HashMap<>();
        response.put("message", "아이템이 삭제되었습니다.");
        return ResponseEntity.ok(response);
    }

    /**
     * 냉장실 아이템 검색
     */
    @GetMapping("/search")
    public ResponseEntity<List<FridgeItemDTO>> searchItems(
        @PathVariable Long fridgeId,
        Authentication authentication,
        @RequestParam String keyword) {

        List<FridgeItemDTO> items = fridgeItemService.searchItems(fridgeId, currentUser.id(authentication), keyword);
        return ResponseEntity.ok(items);
    }

    /**
     * 유통기한 임박 아이템 조회
     */
    @GetMapping("/expiring-soon")
    public ResponseEntity<List<FridgeItemDTO>> getExpiringSoonItems(
        @PathVariable Long fridgeId,
        Authentication authentication,
        @RequestParam(defaultValue = "3") int days) {

        List<FridgeItemDTO> items = fridgeItemService.getExpiringSoonItems(fridgeId, currentUser.id(authentication), days);
        return ResponseEntity.ok(items);
    }

    /**
     * 만료된 아이템 조회
     */
    @GetMapping("/expired")
    public ResponseEntity<List<FridgeItemDTO>> getExpiredItems(
        @PathVariable Long fridgeId,
        Authentication authentication) {

        List<FridgeItemDTO> items = fridgeItemService.getExpiredItems(fridgeId, currentUser.id(authentication));
        return ResponseEntity.ok(items);
    }
}