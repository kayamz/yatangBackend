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

    List<IngredientCatalogEntry> findByUserIsNullAndIconImageFileIsNotNull();

    /** 직접 추가 재료: 해당 사용자만 (Spring 파생 쿼리 findByUserId 대신 명시 JPQL로 소유권 고정) */
    @Query(
            "SELECT e FROM IngredientCatalogEntry e WHERE e.user IS NOT NULL AND e.user.id = :userId ORDER BY e.name ASC")
    List<IngredientCatalogEntry> findCustomEntriesForUser(@Param("userId") Long userId);

    @Query(
            "SELECT e FROM IngredientCatalogEntry e WHERE e.user IS NOT NULL AND e.user.id = :userId "
                    + "AND LOWER(e.name) LIKE LOWER(CONCAT('%', :q, '%')) ORDER BY e.name ASC")
    List<IngredientCatalogEntry> findCustomEntriesForUserAndNameContaining(
            @Param("userId") Long userId, @Param("q") String q);

    @Query(
            "SELECT COUNT(e) > 0 FROM IngredientCatalogEntry e WHERE e.user IS NOT NULL AND e.user.id = :userId "
                    + "AND LOWER(e.name) = LOWER(:name)")
    boolean existsCustomEntryForUserByNameIgnoreCase(@Param("userId") Long userId, @Param("name") String name);

    boolean existsByUserIsNullAndNameIgnoreCase(String name);

    @Query(
            "SELECT COUNT(e) > 0 FROM IngredientCatalogEntry e WHERE e.id = :id AND e.user IS NOT NULL "
                    + "AND e.user.id = :userId")
    boolean existsByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM IngredientCatalogEntry e WHERE e.id = :id AND e.user IS NOT NULL AND e.user.id = :userId")
    void deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
