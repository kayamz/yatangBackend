package com.kaya.yatang.db.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(
        name = "ai_suggest_daily_usage",
        uniqueConstraints = @UniqueConstraint(columnNames = {"actor_key", "usage_date"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiSuggestDailyUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_key", nullable = false, length = 80)
    private String actorKey;

    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    @Column(name = "used_count", nullable = false)
    @Builder.Default
    private int usedCount = 0;
}
