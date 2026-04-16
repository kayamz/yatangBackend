package com.kaya.yatang.service;

import com.kaya.yatang.code.LoginType;
import com.kaya.yatang.db.entity.Fridge;
import com.kaya.yatang.db.entity.User;
import com.kaya.yatang.db.repository.FridgeRepository;
import com.kaya.yatang.db.repository.UserRepository;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OAuthAccountService {

    private static final String OAUTH_PASSWORD_PLACEHOLDER = "OAUTH_NO_PASSWORD";

    private final UserRepository userRepository;
    private final FridgeRepository fridgeRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User findOrCreateFromOAuth(String registrationId, OAuth2User oauth2User) {
        LoginType loginType = toLoginType(registrationId);
        String socialId = extractSocialId(registrationId, oauth2User.getAttributes());
        Optional<User> existing = userRepository.findByLoginTypeAndSocialId(loginType, socialId);
        if (existing.isPresent()) {
            return existing.get();
        }

        String email = extractEmail(registrationId, oauth2User.getAttributes(), socialId, loginType);
        String nickname = extractNickname(registrationId, oauth2User.getAttributes());
        String username = buildUsername(loginType, socialId);

        // username 충돌 시 (극히 드묾) 접미사
        String u = username;
        int n = 0;
        while (userRepository.existsByUsername(u)) {
            n++;
            u = username + "_" + n;
        }
        username = u;

        while (userRepository.existsByEmail(email)) {
            email = "dup_" + UUID.randomUUID().toString().substring(0, 8) + "_" + email;
        }

        if (nickname == null || nickname.isBlank()) {
            nickname = generateTempNickname();
        }
        while (userRepository.existsByNickname(nickname)) {
            nickname = nickname + "_" + UUID.randomUUID().toString().substring(0, 4);
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(OAUTH_PASSWORD_PLACEHOLDER + UUID.randomUUID()));
        user.setNickname(nickname);
        user.setLoginType(loginType);
        user.setSocialId(socialId);

        User saved = userRepository.save(user);

        Fridge mainFridge = new Fridge();
        mainFridge.setName("메인 냉장고");
        mainFridge.setDescription("소셜 로그인 시 자동으로 생성된 냉장고입니다.");
        mainFridge.setUser(saved);
        mainFridge.setIsMain(true);
        fridgeRepository.save(mainFridge);

        return saved;
    }

    private LoginType toLoginType(String registrationId) {
        if ("google".equalsIgnoreCase(registrationId)) {
            return LoginType.GOOGLE;
        }
        if ("kakao".equalsIgnoreCase(registrationId)) {
            return LoginType.KAKAO;
        }
        throw new IllegalArgumentException("지원하지 않는 OAuth 공급자입니다: " + registrationId);
    }

    private String extractSocialId(String registrationId, Map<String, Object> attrs) {
        if ("google".equalsIgnoreCase(registrationId)) {
            Object sub = attrs.get("sub");
            if (sub == null) {
                throw new IllegalStateException("Google OAuth: sub 없음");
            }
            return String.valueOf(sub);
        }
        if ("kakao".equalsIgnoreCase(registrationId)) {
            Object id = attrs.get("id");
            if (id == null) {
                throw new IllegalStateException("Kakao OAuth: id 없음");
            }
            return String.valueOf(id);
        }
        throw new IllegalArgumentException("지원하지 않는 OAuth 공급자입니다.");
    }

    @SuppressWarnings("unchecked")
    private String extractEmail(String registrationId, Map<String, Object> attrs, String socialId, LoginType type) {
        if ("google".equalsIgnoreCase(registrationId)) {
            Object email = attrs.get("email");
            if (email != null && !String.valueOf(email).isBlank()) {
                return String.valueOf(email).trim().toLowerCase(Locale.ROOT);
            }
        }
        if ("kakao".equalsIgnoreCase(registrationId)) {
            Object kacc = attrs.get("kakao_account");
            if (kacc instanceof Map) {
                Object email = ((Map<String, Object>) kacc).get("email");
                if (email != null && !String.valueOf(email).isBlank()) {
                    return String.valueOf(email).trim().toLowerCase(Locale.ROOT);
                }
            }
        }
        return type.name().toLowerCase(Locale.ROOT) + "_" + socialId + "@oauth.yatang.local";
    }

    @SuppressWarnings("unchecked")
    private String extractNickname(String registrationId, Map<String, Object> attrs) {
        if ("google".equalsIgnoreCase(registrationId)) {
            Object name = attrs.get("name");
            if (name != null && !String.valueOf(name).isBlank()) {
                return String.valueOf(name).trim();
            }
        }
        if ("kakao".equalsIgnoreCase(registrationId)) {
            Object kacc = attrs.get("kakao_account");
            if (kacc instanceof Map) {
                Object profile = ((Map<String, Object>) kacc).get("profile");
                if (profile instanceof Map) {
                    Object nick = ((Map<String, Object>) profile).get("nickname");
                    if (nick != null && !String.valueOf(nick).isBlank()) {
                        return String.valueOf(nick).trim();
                    }
                }
            }
        }
        return null;
    }

    private String buildUsername(LoginType type, String socialId) {
        String prefix = type == LoginType.GOOGLE ? "g" : "k";
        String safe = socialId.replaceAll("[^a-zA-Z0-9]", "");
        if (safe.length() > 40) {
            safe = safe.substring(0, 40);
        }
        return prefix + "_" + safe;
    }

    private String generateTempNickname() {
        String tempNickname;
        do {
            String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            tempNickname = "user_" + uuid;
        } while (userRepository.existsByNickname(tempNickname));
        return tempNickname;
    }
}
