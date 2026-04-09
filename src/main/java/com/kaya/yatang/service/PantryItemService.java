package com.kaya.yatang.service;

import com.kaya.yatang.db.entity.PantryItem;
import com.kaya.yatang.db.entity.User;
import com.kaya.yatang.db.repository.PantryItemRepository;
import com.kaya.yatang.db.repository.UserRepository;
import com.kaya.yatang.dto.PantryItemDTO;
import com.kaya.yatang.dto.request.ItemRequest;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PantryItemService {

    private final PantryItemRepository pantryItemRepository;
    private final UserRepository userRepository;

    public PantryItemDTO createItem(Long userId, ItemRequest request) {
        User user = userRepository.findById(Objects.requireNonNull(userId, "userId"))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        PantryItem item = PantryItem.builder()
                .user(user)
                .name(request.getName())
                .quantity(request.getQuantity() != null ? request.getQuantity() : 1)
                .unit(request.getUnit() != null ? request.getUnit() : "개")
                .expirationDate(request.getExpirationDate())
                .manufactureDate(request.getManufactureDate())
                .memo(request.getMemo())
                .build();
        return new PantryItemDTO(pantryItemRepository.save(item));
    }

    @Transactional(readOnly = true)
    public List<PantryItemDTO> getItems(Long userId) {
        assertUser(userId);
        return pantryItemRepository.findByUser_Id(userId).stream()
                .map(PantryItemDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PantryItemDTO getItemById(Long itemId, Long userId) {
        PantryItem item = pantryItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이템입니다."));
        if (!item.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 아이템에 접근할 권한이 없습니다.");
        }
        return new PantryItemDTO(item);
    }

    public PantryItemDTO updateItem(Long itemId, Long userId, ItemRequest request) {
        PantryItem item = pantryItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이템입니다."));
        if (!item.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 아이템에 접근할 권한이 없습니다.");
        }
        if (request.getName() != null) {
            item.setName(request.getName());
        }
        if (request.getQuantity() != null) {
            item.setQuantity(request.getQuantity());
        }
        if (request.getUnit() != null) {
            item.setUnit(request.getUnit());
        }
        if (request.getExpirationDate() != null) {
            item.setExpirationDate(request.getExpirationDate());
        }
        if (request.getManufactureDate() != null) {
            item.setManufactureDate(request.getManufactureDate());
        }
        if (request.getMemo() != null) {
            item.setMemo(request.getMemo());
        }
        return new PantryItemDTO(pantryItemRepository.save(item));
    }

    public void deleteItem(Long itemId, Long userId) {
        PantryItem item = pantryItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이템입니다."));
        if (!item.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 아이템에 접근할 권한이 없습니다.");
        }
        pantryItemRepository.delete(item);
    }

    private void assertUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }
    }
}
