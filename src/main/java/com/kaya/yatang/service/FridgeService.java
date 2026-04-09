package com.kaya.yatang.service;

import com.kaya.yatang.db.entity.Fridge;
import com.kaya.yatang.db.entity.FreezerItem;
import com.kaya.yatang.db.entity.FridgeItem;
import com.kaya.yatang.db.entity.PantryItem;
import com.kaya.yatang.db.repository.FridgeRepository;
import com.kaya.yatang.db.entity.User;
import com.kaya.yatang.db.repository.PantryItemRepository;
import com.kaya.yatang.db.repository.UserRepository;
import com.kaya.yatang.dto.AggregatedItemDTO;
import com.kaya.yatang.dto.FridgeDTO;
import com.kaya.yatang.dto.FridgeStatsDTO;
import com.kaya.yatang.dto.ItemSummaryDTO;
import com.kaya.yatang.dto.request.FridgeRequest;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class FridgeService {

    private final FridgeRepository fridgeRepository;
    private final UserRepository userRepository;
    private final PantryItemRepository pantryItemRepository;

    /**
     * 냉장고 생성
     */
    public FridgeDTO createFridge(Long userId, FridgeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // if (request.getIsMain() == true) {
        //
        // }
        if (Boolean.TRUE.equals(request.getIsMain())) {
            user.getFridges().forEach(f -> {
                if (Boolean.TRUE.equals(f.getIsMain())) {
                    f.setIsMain(false);
                }
            });
        }
        Fridge fridge = Fridge.createFridge(user, request.getIsMain() != null ? request.getIsMain() : false,
                request.getName(), request.getDescription());
        Fridge savedFridge = fridgeRepository.save(fridge);

        return new FridgeDTO(savedFridge);
    }

    /**
     * 냉장고 목록 조회 (특정 사용자의)
     */
    @Transactional(readOnly = true)
    public List<FridgeDTO> getUserFridges(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        return user.getFridges().stream()
                .map(FridgeDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * 냉장고 상세 조회
     */
    @Transactional(readOnly = true)
    public FridgeDTO getFridgeById(Long fridgeId, Long userId) {
        Fridge fridge = fridgeRepository.findById(fridgeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 냉장고입니다."));

        // 소유자 확인
        if (!fridge.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 냉장고에 접근할 권한이 없습니다.");
        }

        return new FridgeDTO(fridge);
    }

    /**
     * 메인 냉장고 조회
     */
    @Transactional(readOnly = true)
    public FridgeDTO getMainFridge(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // "메인 냉장고" 찾기
        Fridge mainFridge = user.getFridges().stream()
                .filter(fridge -> Boolean.TRUE.equals(fridge.getIsMain()))
                .findFirst()
                .orElseGet(() -> user.getFridges().isEmpty() ? null : user.getFridges().get(0));

        if (mainFridge == null) {
            throw new IllegalArgumentException("냉장고가 존재하지 않습니다.");
        }

        return new FridgeDTO(mainFridge);
    }

    /**
     * 냉장고 정보 수정
     */
    public FridgeDTO updateFridge(Long fridgeId, Long userId, FridgeRequest request) {
        Fridge fridge = fridgeRepository.findById(fridgeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 냉장고입니다."));

        // 소유자 확인
        if (!fridge.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 냉장고를 수정할 권한이 없습니다.");
        }

        // 정보 수정
        if (request.getName() != null) {
            fridge.setName(request.getName());
        }
        if (request.getDescription() != null) {
            fridge.setDescription(request.getDescription());
        }
        if (request.getIsMain() != null) {
            if (Boolean.TRUE.equals(request.getIsMain()) && !Boolean.TRUE.equals(fridge.getIsMain())) {
                fridge.getUser().getFridges().forEach(f -> {
                    if (Boolean.TRUE.equals(f.getIsMain())) {
                        f.setIsMain(false);
                    }
                });
            }
            fridge.setIsMain(request.getIsMain());
        }

        Fridge updatedFridge = fridgeRepository.save(fridge);
        return new FridgeDTO(updatedFridge);
    }

    /**
     * 냉장고 삭제
     */
    public void deleteFridge(Long fridgeId, Long userId) {
        Fridge fridge = fridgeRepository.findById(fridgeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 냉장고입니다."));

        // 소유자 확인
        if (!fridge.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 냉장고를 삭제할 권한이 없습니다.");
        }

        // 메인 냉장고는 삭제 불가
        if (Boolean.TRUE.equals(fridge.getIsMain())) {
            throw new IllegalArgumentException("메인 냉장고는 삭제할 수 없습니다.");
        }

        fridgeRepository.delete(fridge);
    }

    /**
     * 냉장고 통계 조회
     */
    @Transactional(readOnly = true)
    public FridgeStatsDTO getFridgeStats(Long fridgeId, Long userId) {
        Fridge fridge = fridgeRepository.findById(fridgeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 냉장고입니다."));

        // 소유자 확인
        if (!fridge.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 냉장고에 접근할 권한이 없습니다.");
        }

        return new FridgeStatsDTO(fridge);
    }

    /**
     * 유통기한 임박 아이템 조회
     */
    @Transactional(readOnly = true)
    public List<ItemSummaryDTO> getExpiringSoonItems(Long fridgeId, Long userId, int days) {
        Fridge fridge = fridgeRepository.findById(fridgeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 냉장고입니다."));

        // 소유자 확인
        if (!fridge.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 냉장고에 접근할 권한이 없습니다.");
        }

        List<ItemSummaryDTO> expiringSoonItems = new ArrayList<>();

        // 냉장실 아이템
        fridge.getFridgeItems().stream()
                .filter(item -> item.isExpiringSoon(days))
                .forEach(item -> expiringSoonItems.add(new ItemSummaryDTO(item, "냉장실")));

        // 냉동실 아이템
        fridge.getFreezerItems().stream()
                .filter(item -> item.getExpirationDate() != null)
                .filter(item -> ChronoUnit.DAYS.between(LocalDate.now(), item.getExpirationDate()) <= days)
                .forEach(item -> expiringSoonItems.add(new ItemSummaryDTO(item, "냉동실")));

        // 유통기한 임박 순으로 정렬
        expiringSoonItems.sort(Comparator.comparing(ItemSummaryDTO::getDaysUntilExpiration));

        return expiringSoonItems;
    }

    /**
     * 사용자 소유 모든 냉장고의 냉장실·냉동실 아이템을 한 목록으로 반환
     */
    @Transactional(readOnly = true)
    public List<AggregatedItemDTO> getAllItemsAcrossFridges(Long userId) {
        User user = userRepository.findById(Objects.requireNonNull(userId, "userId"))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        List<AggregatedItemDTO> out = new ArrayList<>();
        for (Fridge fridge : user.getFridges()) {
            String fname = fridge.getName();
            for (FridgeItem item : fridge.getFridgeItems()) {
                out.add(AggregatedItemDTO.fromFridgeItem(item, fname));
            }
            for (FreezerItem item : fridge.getFreezerItems()) {
                out.add(AggregatedItemDTO.fromFreezerItem(item, fname));
            }
        }
        for (PantryItem p : pantryItemRepository.findByUser_Id(userId)) {
            out.add(AggregatedItemDTO.fromPantryItem(p));
        }
        out.sort(Comparator.comparing(AggregatedItemDTO::getName, String.CASE_INSENSITIVE_ORDER));
        return out;
    }
}