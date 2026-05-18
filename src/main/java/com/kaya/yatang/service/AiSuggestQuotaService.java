package com.kaya.yatang.service;

import com.kaya.yatang.db.entity.AiSuggestDailyUsage;
import com.kaya.yatang.db.repository.AiSuggestDailyUsageRepository;
import com.kaya.yatang.dto.recipe.AiSuggestQuotaDto;
import com.kaya.yatang.security.CurrentUser;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AiSuggestQuotaService {

    private static final Pattern UUID_PATTERN =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$");

    private final AiSuggestDailyUsageRepository usageRepository;
    private final CurrentUser currentUser;

    @Value("${yatang.ai-suggest.daily-limit-guest:5}")
    private int guestDailyLimit;

    @Value("${yatang.ai-suggest.daily-limit-user:20}")
    private int userDailyLimit;

    /**
     * 클라이언트(브라우저/앱)가 보낸 로컬 달력 날짜. 한도는 이 날짜 기준으로 집계합니다.
     *
     * @param clientLocalDateHeader {@code X-Client-Local-Date}, 형식 {@code YYYY-MM-DD}
     */
    public LocalDate parseClientLocalDate(String clientLocalDateHeader) {
        if (clientLocalDateHeader == null || clientLocalDateHeader.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "AI 추천 한도 집계를 위해 X-Client-Local-Date 헤더(YYYY-MM-DD, 기기 로컬 날짜)가 필요합니다.");
        }
        String raw = clientLocalDateHeader.trim();
        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "X-Client-Local-Date는 YYYY-MM-DD 형식이어야 합니다.");
        }
    }

    public AiSuggestQuotaDto getQuota(Authentication authentication, String guestSessionId, LocalDate usageDate) {
        Actor actor = resolveActor(authentication, guestSessionId);
        int limit = actor.user ? userDailyLimit : guestDailyLimit;
        int used = usageRepository
                .findByActorKeyAndUsageDate(actor.key, usageDate)
                .map(AiSuggestDailyUsage::getUsedCount)
                .orElse(0);
        int remaining = Math.max(0, limit - used);
        return new AiSuggestQuotaDto(used, limit, remaining);
    }

    public void assertCanSuggest(Authentication authentication, String guestSessionId, LocalDate usageDate) {
        AiSuggestQuotaDto q = getQuota(authentication, guestSessionId, usageDate);
        if (q.getRemainingToday() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS, "오늘(기기 날짜 기준) AI 레시피 추천 횟수를 모두 사용했습니다.");
        }
    }

    @Transactional
    public AiSuggestQuotaDto recordSuccessfulSuggest(Authentication authentication, String guestSessionId, LocalDate usageDate) {
        Actor actor = resolveActor(authentication, guestSessionId);
        int limit = actor.user ? userDailyLimit : guestDailyLimit;

        AiSuggestDailyUsage row = usageRepository
                .findByActorKeyAndUsageDate(actor.key, usageDate)
                .orElseGet(() -> AiSuggestDailyUsage.builder()
                        .actorKey(actor.key)
                        .usageDate(usageDate)
                        .usedCount(0)
                        .build());

        int next = row.getUsedCount() + 1;
        if (next > limit) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS, "오늘(기기 날짜 기준) AI 레시피 추천 횟수를 모두 사용했습니다.");
        }
        row.setUsedCount(next);
        usageRepository.save(row);

        return new AiSuggestQuotaDto(next, limit, Math.max(0, limit - next));
    }

    private record Actor(String key, boolean user) {}

    private Actor resolveActor(Authentication authentication, String guestSessionId) {
        Long userId = currentUser.idOrNull(authentication);
        if (userId != null) {
            return new Actor("u:" + userId, true);
        }

        String guest = guestSessionId == null ? "" : guestSessionId.trim();
        if (guest.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "비회원 이용 시 X-Guest-Session-Id 헤더(기기별 UUID)가 필요합니다.");
        }
        if (guest.length() > 64 || !UUID_PATTERN.matcher(guest).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "X-Guest-Session-Id 형식이 올바르지 않습니다.");
        }
        try {
            UUID.fromString(guest);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "X-Guest-Session-Id 형식이 올바르지 않습니다.");
        }

        return new Actor("g:" + guest.toLowerCase(), false);
    }
}
