package com.kaya.yatang.db.repository;

import com.kaya.yatang.db.entity.IngredientCatalogEntry;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IngredientCatalogEntryRepository extends JpaRepository<IngredientCatalogEntry, Long> {

    long countByUserIsNull();

    List<IngredientCatalogEntry> findByUserIsNullAndNameContainingIgnoreCaseOrderByNameAsc(String q);

    List<IngredientCatalogEntry> findByUserIsNullOrderByNameAsc();

    List<IngredientCatalogEntry> findByUserIdAndNameContainingIgnoreCaseOrderByNameAsc(Long userId, String q);

    List<IngredientCatalogEntry> findByUserIdOrderByNameAsc(Long userId);

    boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);
}
