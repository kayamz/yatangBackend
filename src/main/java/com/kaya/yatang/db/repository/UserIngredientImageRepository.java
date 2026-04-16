package com.kaya.yatang.db.repository;

import com.kaya.yatang.db.entity.UserIngredientImage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserIngredientImageRepository extends JpaRepository<UserIngredientImage, Long> {

    List<UserIngredientImage> findByUserId(Long userId);

    @Query(
            "SELECT u FROM UserIngredientImage u WHERE u.user.id = :userId AND "
                    + "LOWER(TRIM(u.ingredientName)) = LOWER(TRIM(:ingredientName))")
    Optional<UserIngredientImage> findByUserIdAndIngredientNameIgnoreCase(
            @Param("userId") Long userId, @Param("ingredientName") String ingredientName);

    Optional<UserIngredientImage> findByPublicId(String publicId);
}
