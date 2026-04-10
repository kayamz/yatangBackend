package com.kaya.yatang.db.repository;

import com.kaya.yatang.db.entity.SavedRecipe;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavedRecipeRepository extends JpaRepository<SavedRecipe, Long> {

    List<SavedRecipe> findByUser_IdOrderByCreatedAtDesc(Long userId);

    Optional<SavedRecipe> findByIdAndUser_Id(Long id, Long userId);

    void deleteByIdAndUser_Id(Long id, Long userId);
}
