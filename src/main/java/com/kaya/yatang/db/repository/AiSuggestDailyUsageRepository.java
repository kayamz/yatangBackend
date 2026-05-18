package com.kaya.yatang.db.repository;

import com.kaya.yatang.db.entity.AiSuggestDailyUsage;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiSuggestDailyUsageRepository extends JpaRepository<AiSuggestDailyUsage, Long> {

    Optional<AiSuggestDailyUsage> findByActorKeyAndUsageDate(String actorKey, LocalDate usageDate);
}
