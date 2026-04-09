package com.kaya.yatang.db.repository;

import com.kaya.yatang.db.entity.IngredientCatalogEntry;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IngredientCatalogEntryRepository extends JpaRepository<IngredientCatalogEntry, Long> {

    long countByUserIsNull();

    List<IngredientCatalogEntry> findByUserIsNullAndNameContainingIgnoreCaseOrderByNameAsc(String q);

    List<IngredientCatalogEntry> findByUserIsNullOrderByNameAsc();

    List<IngredientCatalogEntry> findByUserIdAndNameContainingIgnoreCaseOrderByNameAsc(Long userId, String q);

    List<IngredientCatalogEntry> findByUserIdOrderByNameAsc(Long userId);

    boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);

    boolean existsByUserIsNullAndNameIgnoreCase(String name);

    @Query("SELECT COUNT(e) > 0 FROM IngredientCatalogEntry e WHERE e.id = :id AND e.user.id = :userId")
    boolean existsByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM IngredientCatalogEntry e WHERE e.id = :id AND e.user.id = :userId")
    void deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
