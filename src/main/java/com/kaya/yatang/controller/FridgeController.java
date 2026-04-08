package com.kaya.yatang.controller;

import com.kaya.yatang.dto.AggregatedItemDTO;
import com.kaya.yatang.dto.FridgeDTO;
import com.kaya.yatang.dto.FridgeStatsDTO;
import com.kaya.yatang.dto.ItemSummaryDTO;
import com.kaya.yatang.dto.request.FridgeRequest;
import com.kaya.yatang.service.FridgeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fridges")
@RequiredArgsConstructor
public class FridgeController {

    private final FridgeService fridgeService;

    /**
     * 냉장고 생성
     */
    @PostMapping
    public ResponseEntity<FridgeDTO> createFridge(
            @RequestParam Long userId,
            @RequestBody FridgeRequest request) {

        FridgeDTO fridge = fridgeService.createFridge(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(fridge);
    }

    /**
     * 내 냉장고 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<FridgeDTO>> getUserFridges(@RequestParam Long userId) {
        List<FridgeDTO> fridges = fridgeService.getUserFridges(userId);
        return ResponseEntity.ok(fridges);
    }

    /**
     * 메인 냉장고 조회
     */
    @GetMapping("/main")
    public ResponseEntity<FridgeDTO> getMainFridge(@RequestParam Long userId) {
        FridgeDTO mainFridge = fridgeService.getMainFridge(userId);
        return ResponseEntity.ok(mainFridge);
    }

    /**
     * 모든 냉장고의 재료 전체 보기 (냉장실 + 냉동실)
     */
    @GetMapping("/all-items")
    public ResponseEntity<List<AggregatedItemDTO>> getAllItemsAcrossFridges(@RequestParam Long userId) {
        return ResponseEntity.ok(fridgeService.getAllItemsAcrossFridges(userId));
    }

    /**
     * 냉장고 상세 조회
     */
    @GetMapping("/{fridgeId}")
    public ResponseEntity<FridgeDTO> getFridgeById(
            @PathVariable Long fridgeId,
            @RequestParam Long userId) {

        FridgeDTO fridge = fridgeService.getFridgeById(fridgeId, userId);
        return ResponseEntity.ok(fridge);
    }

    /**
     * 냉장고 정보 수정
     */
    @PatchMapping("/{fridgeId}")
    public ResponseEntity<FridgeDTO> updateFridge(
            @PathVariable Long fridgeId,
            @RequestParam Long userId,
            @RequestBody FridgeRequest request) {

        FridgeDTO updatedFridge = fridgeService.updateFridge(fridgeId, userId, request);
        return ResponseEntity.ok(updatedFridge);
    }

    /**
     * 냉장고 삭제
     */
    @DeleteMapping("/{fridgeId}")
    public ResponseEntity<Map<String, String>> deleteFridge(
            @PathVariable Long fridgeId,
            @RequestParam Long userId) {

        fridgeService.deleteFridge(fridgeId, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "냉장고가 삭제되었습니다.");

        return ResponseEntity.ok(response);
    }

    /**
     * 냉장고 통계 조회
     */
    @GetMapping("/{fridgeId}/stats")
    public ResponseEntity<FridgeStatsDTO> getFridgeStats(
            @PathVariable Long fridgeId,
            @RequestParam Long userId) {

        FridgeStatsDTO stats = fridgeService.getFridgeStats(fridgeId, userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * 유통기한 임박 아이템 조회
     */
    @GetMapping("/{fridgeId}/expiring-soon")
    public ResponseEntity<List<ItemSummaryDTO>> getExpiringSoonItems(
            @PathVariable Long fridgeId,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "3") int days) {

        List<ItemSummaryDTO> items = fridgeService.getExpiringSoonItems(fridgeId, userId, days);
        return ResponseEntity.ok(items);
    }
}