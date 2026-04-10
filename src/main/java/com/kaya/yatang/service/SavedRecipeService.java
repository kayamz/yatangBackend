package com.kaya.yatang.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaya.yatang.db.entity.SavedRecipe;
import com.kaya.yatang.db.entity.User;
import com.kaya.yatang.db.repository.SavedRecipeRepository;
import com.kaya.yatang.db.repository.UserRepository;
import com.kaya.yatang.dto.recipe.AiRecipeDto;
import com.kaya.yatang.dto.recipe.SavedRecipeDetailDTO;
import com.kaya.yatang.dto.recipe.SavedRecipeSummaryDTO;
import com.kaya.yatang.dto.request.RecipeBookSaveRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SavedRecipeService {

    private final SavedRecipeRepository savedRecipeRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<SavedRecipeSummaryDTO> list(Long userId) {
        return savedRecipeRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SavedRecipeDetailDTO get(Long userId, Long id) {
        SavedRecipe r = savedRecipeRepository
                .findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("레시피를 찾을 수 없습니다."));
        return toDetail(r);
    }

    @Transactional
    public SavedRecipeSummaryDTO save(Long userId, RecipeBookSaveRequest request) {
        AiRecipeDto recipe = request.getRecipe();
        if (recipe == null || recipe.getTitle() == null || recipe.getTitle().isBlank()) {
            throw new IllegalArgumentException("저장할 레시피가 없습니다.");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        String json;
        try {
            json = objectMapper.writeValueAsString(recipe);
        } catch (Exception e) {
            throw new IllegalStateException("레시피 직렬화 실패", e);
        }
        SavedRecipe entity = SavedRecipe.builder()
                .user(user)
                .title(recipe.getTitle().trim())
                .servings(recipe.getServings())
                .cookMinutes(recipe.getCookMinutes())
                .payloadJson(json)
                .createdAt(LocalDateTime.now())
                .build();
        return toSummary(savedRecipeRepository.save(entity));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        savedRecipeRepository.deleteByIdAndUser_Id(id, userId);
    }

    private SavedRecipeSummaryDTO toSummary(SavedRecipe e) {
        return SavedRecipeSummaryDTO.builder()
                .id(e.getId())
                .title(e.getTitle())
                .servings(e.getServings())
                .cookMinutes(e.getCookMinutes())
                .createdAt(e.getCreatedAt())
                .build();
    }

    private SavedRecipeDetailDTO toDetail(SavedRecipe e) {
        return SavedRecipeDetailDTO.builder()
                .id(e.getId())
                .title(e.getTitle())
                .servings(e.getServings())
                .cookMinutes(e.getCookMinutes())
                .payloadJson(e.getPayloadJson())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
