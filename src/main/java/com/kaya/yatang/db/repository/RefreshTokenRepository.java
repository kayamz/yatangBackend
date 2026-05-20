package com.kaya.yatang.db.repository;

import com.kaya.yatang.db.entity.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update RefreshToken rt set rt.revokedAt = CURRENT_TIMESTAMP where rt.user.id = :userId and rt.revokedAt is null")
    int revokeAllActiveByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("update RefreshToken rt set rt.revokedAt = CURRENT_TIMESTAMP where rt.tokenHash = :tokenHash and rt.revokedAt is null")
    int revokeByTokenHash(@Param("tokenHash") String tokenHash);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
    void deleteAllByUser_Id(@Param("userId") Long userId);
}
