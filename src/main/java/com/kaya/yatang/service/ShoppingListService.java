package com.kaya.yatang.service;

import com.kaya.yatang.db.entity.ShoppingListItem;
import com.kaya.yatang.db.entity.User;
import com.kaya.yatang.db.repository.ShoppingListItemRepository;
import com.kaya.yatang.db.repository.UserRepository;
import com.kaya.yatang.dto.recipe.ShoppingListItemDTO;
import com.kaya.yatang.dto.request.ShoppingListAddLineRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShoppingListService {

    private final ShoppingListItemRepository shoppingListItemRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ShoppingListItemDTO> list(Long userId) {
        return shoppingListItemRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ShoppingListItemDTO add(Long userId, ShoppingListAddLineRequest req) {
        if (req.getIngredientName() == null || req.getIngredientName().isBlank()) {
            throw new IllegalArgumentException("재료 이름을 입력해주세요.");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        ShoppingListItem item = ShoppingListItem.builder()
                .user(user)
                .ingredientName(req.getIngredientName().trim())
                .quantityNote(req.getQuantityNote() != null ? req.getQuantityNote().trim() : null)
                .unit(req.getUnit() != null ? req.getUnit().trim() : null)
                .checked(false)
                .createdAt(LocalDateTime.now())
                .build();
        return toDto(shoppingListItemRepository.save(item));
    }

    @Transactional
    public List<ShoppingListItemDTO> addBatch(Long userId, List<ShoppingListAddLineRequest> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("담을 재료가 없습니다.");
        }
        return lines.stream().map(line -> add(userId, line)).collect(Collectors.toList());
    }

    @Transactional
    public ShoppingListItemDTO toggleChecked(Long userId, Long itemId) {
        ShoppingListItem item = shoppingListItemRepository
                .findById(itemId)
                .filter(i -> i.getUser().getId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("항목을 찾을 수 없습니다."));
        item.setChecked(!item.isChecked());
        return toDto(item);
    }

    @Transactional
    public void delete(Long userId, Long itemId) {
        shoppingListItemRepository.deleteByIdAndUser_Id(itemId, userId);
    }

    private ShoppingListItemDTO toDto(ShoppingListItem e) {
        return ShoppingListItemDTO.builder()
                .id(e.getId())
                .ingredientName(e.getIngredientName())
                .quantityNote(e.getQuantityNote())
                .unit(e.getUnit())
                .checked(e.isChecked())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
