package com.kaya.yatang.service;

import com.kaya.yatang.db.entity.IngredientCatalogEntry;
import com.kaya.yatang.db.entity.User;
import com.kaya.yatang.db.repository.IngredientCatalogEntryRepository;
import com.kaya.yatang.db.repository.UserRepository;
import com.kaya.yatang.dto.IngredientCatalogDTO;
import com.kaya.yatang.dto.request.IngredientCatalogCreateRequest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    private final UserIngredientImageService userIngredientImageService;

    @Transactional(readOnly = true)
    public List<IngredientCatalogDTO> listCatalog(String q, Long userId, String category) {
        String keyword = q == null ? "" : q.trim();
        String cat = category == null ? "" : category.trim();
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

        if (!cat.isEmpty() && !"전체".equals(cat)) {
            final String filter = cat;
            merged = merged.stream()
                    .filter(e -> {
                        if ("직접 추가".equals(filter)) {
                            return e.getUser() != null;
                        }
                        if (e.getUser() != null) {
                            return false;
                        }
                        if ("기타".equals(filter)) {
                            return e.getCategory() == null || e.getCategory().isBlank();
                        }
                        return e.getCategory() != null && filter.equals(e.getCategory());
                    })
                    .collect(Collectors.toList());
        }

        merged.sort(Comparator.comparing(IngredientCatalogEntry::getName, String.CASE_INSENSITIVE_ORDER));
        List<IngredientCatalogDTO> list =
                merged.stream().map(IngredientCatalogDTO::new).collect(Collectors.toList());
        if (userId != null) {
            Map<String, String> publicIdByName = userIngredientImageService.getPublicIdByIngredientNameLower(userId);
            for (IngredientCatalogDTO dto : list) {
                if (dto.getName() == null || !dto.isCustom()) {
                    continue;
                }
                String pid = publicIdByName.get(dto.getName().trim().toLowerCase(Locale.ROOT));
                if (pid != null) {
                    dto.setUserImageUrl("/api/public/ingredient-images/" + pid);
                }
            }
        }
        return list;
    }

    /**
     * 시스템 카탈로그에 아이콘 파일명이 있는 항목만: 재료명(소문자) → 파일명(또는 상대경로).
     * 프론트에서 public URL로 조합합니다.
     */
    @Transactional(readOnly = true)
    public Map<String, String> getSystemIconFileByNameLower() {
        Map<String, String> map = new HashMap<>();
        for (IngredientCatalogEntry e : catalogRepository.findByUserIsNullAndIconImageFileIsNotNull()) {
            if (e.getName() == null) {
                continue;
            }
            String file = e.getIconImageFile();
            if (file == null) {
                continue;
            }
            file = file.trim();
            if (file.isEmpty()) {
                continue;
            }
            map.put(e.getName().trim().toLowerCase(Locale.ROOT), file);
        }
        return map;
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
        if (catalogRepository.existsByUserIsNullAndNameIgnoreCase(name)) {
            throw new IllegalArgumentException("이미 목록에 있는 재료입니다.");
        }
        if (catalogRepository.existsByUserIdAndNameIgnoreCase(userId, name)) {
            throw new IllegalArgumentException("이미 목록에 있는 재료입니다.");
        }
        IngredientCatalogEntry saved = catalogRepository.save(IngredientCatalogEntry.builder()
                .name(name)
                .defaultUnit(unit)
                .category(null)
                .user(user)
                .build());
        return new IngredientCatalogDTO(saved);
    }

    public void deleteCustom(Long userId, Long entryId) {
        if (entryId == null) {
            throw new IllegalArgumentException("항목을 찾을 수 없습니다.");
        }
        if (!catalogRepository.existsByIdAndUserId(entryId, userId)) {
            throw new IllegalArgumentException("삭제할 수 없는 항목입니다.");
        }
        catalogRepository.deleteByIdAndUserId(entryId, userId);
    }
}
