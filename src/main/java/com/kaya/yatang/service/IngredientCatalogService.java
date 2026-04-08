package com.kaya.yatang.service;

import com.kaya.yatang.db.entity.IngredientCatalogEntry;
import com.kaya.yatang.db.entity.User;
import com.kaya.yatang.db.repository.IngredientCatalogEntryRepository;
import com.kaya.yatang.db.repository.UserRepository;
import com.kaya.yatang.dto.IngredientCatalogDTO;
import com.kaya.yatang.dto.request.IngredientCatalogCreateRequest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class IngredientCatalogService {

    private final IngredientCatalogEntryRepository catalogRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<IngredientCatalogDTO> listCatalog(String q, Long userId) {
        String keyword = q == null ? "" : q.trim();
        List<IngredientCatalogEntry> system;
        if (keyword.isEmpty()) {
            system = catalogRepository.findByUserIsNullOrderByNameAsc();
        } else {
            system = catalogRepository.findByUserIsNullAndNameContainingIgnoreCaseOrderByNameAsc(keyword);
        }

        List<IngredientCatalogEntry> merged = new ArrayList<>(system);
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                List<IngredientCatalogEntry> mine;
                if (keyword.isEmpty()) {
                    mine = catalogRepository.findByUserIdOrderByNameAsc(userId);
                } else {
                    mine = catalogRepository.findByUserIdAndNameContainingIgnoreCaseOrderByNameAsc(userId, keyword);
                }
                merged.addAll(mine);
            }
        }

        merged.sort(Comparator.comparing(IngredientCatalogEntry::getName, String.CASE_INSENSITIVE_ORDER));
        return merged.stream().map(IngredientCatalogDTO::new).collect(Collectors.toList());
    }

    public IngredientCatalogDTO addCustom(Long userId, IngredientCatalogCreateRequest request) {
        if (request == null || request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("재료 이름을 입력해주세요.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        String name = request.getName().trim();
        String unit = request.getDefaultUnit() != null && !request.getDefaultUnit().trim().isEmpty()
                ? request.getDefaultUnit().trim()
                : "개";
        if (catalogRepository.existsByUserIdAndNameIgnoreCase(userId, name)) {
            throw new IllegalArgumentException("이미 목록에 있는 재료입니다.");
        }
        IngredientCatalogEntry saved = catalogRepository.save(IngredientCatalogEntry.builder()
                .name(name)
                .defaultUnit(unit)
                .user(user)
                .build());
        return new IngredientCatalogDTO(saved);
    }
}
