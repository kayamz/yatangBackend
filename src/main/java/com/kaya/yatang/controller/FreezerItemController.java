package com.kaya.yatang.controller;

import com.kaya.yatang.dto.FreezerItemDTO;
import com.kaya.yatang.dto.request.ItemRequest;
import com.kaya.yatang.security.CurrentUser;
import com.kaya.yatang.service.FreezerItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fridges/{fridgeId}/freezer-items")
@RequiredArgsConstructor
public class FreezerItemController {

    private final FreezerItemService freezerItemService;
    private final CurrentUser currentUser;

    /**
     * 냉동실 아이템 추가
     */
    @PostMapping
    public ResponseEntity<FreezerItemDTO> createItem(
        @PathVariable Long fridgeId,
        Authentication authentication,
        @RequestBody ItemRequest request) {

        FreezerItemDTO item = freezerItemService.createItem(fridgeId, currentUser.id(authentication), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    /**
     * 냉동실 아이템 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<FreezerItemDTO>> getItems(
        @PathVariable Long fridgeId,
        Authentication authentication) {

        List<FreezerItemDTO> items = freezerItemService.getItemsByFridge(fridgeId, currentUser.id(authentication));
        return ResponseEntity.ok(items);
    }

    /**
     * 냉동실 아이템 상세 조회
     */
    @GetMapping("/{itemId}")
    public ResponseEntity<FreezerItemDTO> getItem(
        @PathVariable Long fridgeId,
        @PathVariable Long itemId,
        Authentication authentication) {

        FreezerItemDTO item = freezerItemService.getItemById(itemId, currentUser.id(authentication));
        return ResponseEntity.ok(item);
    }

    /**
     * 냉동실 아이템 수정
     */
    @PatchMapping("/{itemId}")
    public ResponseEntity<FreezerItemDTO> updateItem(
        @PathVariable Long fridgeId,
        @PathVariable Long itemId,
        Authentication authentication,
        @RequestBody ItemRequest request) {

        FreezerItemDTO item = freezerItemService.updateItem(itemId, currentUser.id(authentication), request);
        return ResponseEntity.ok(item);
    }

    /**
     * 냉동실 아이템 삭제
     */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Map<String, String>> deleteItem(
        @PathVariable Long fridgeId,
        @PathVariable Long itemId,
        Authentication authentication) {

        freezerItemService.deleteItem(itemId, currentUser.id(authentication));

        Map<String, String> response = new HashMap<>();
        response.put("message", "아이템이 삭제되었습니다.");
        return ResponseEntity.ok(response);
    }

    /**
     * 냉동실 아이템 검색
     */
    @GetMapping("/search")
    public ResponseEntity<List<FreezerItemDTO>> searchItems(
        @PathVariable Long fridgeId,
        Authentication authentication,
        @RequestParam String keyword) {

        List<FreezerItemDTO> items = freezerItemService.searchItems(fridgeId, currentUser.id(authentication), keyword);
        return ResponseEntity.ok(items);
    }

    /**
     * 장기 보관 아이템 조회 (6개월 이상)
     */
    @GetMapping("/long-term")
    public ResponseEntity<List<FreezerItemDTO>> getLongTermFrozenItems(
        @PathVariable Long fridgeId,
        Authentication authentication) {

        List<FreezerItemDTO> items = freezerItemService.getLongTermFrozenItems(fridgeId, currentUser.id(authentication));
        return ResponseEntity.ok(items);
    }

    /**
     * 유통기한 임박 아이템 조회
     */
    @GetMapping("/expiring-soon")
    public ResponseEntity<List<FreezerItemDTO>> getExpiringSoonItems(
        @PathVariable Long fridgeId,
        Authentication authentication,
        @RequestParam(defaultValue = "3") int days) {

        List<FreezerItemDTO> items = freezerItemService.getExpiringSoonItems(fridgeId, currentUser.id(authentication), days);
        return ResponseEntity.ok(items);
    }
}