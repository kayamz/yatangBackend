package com.kaya.yatang.service;

import com.kaya.yatang.code.LoginType;
import com.kaya.yatang.db.entity.FreezerItem;
import com.kaya.yatang.db.entity.Fridge;
import com.kaya.yatang.db.entity.FridgeItem;
import com.kaya.yatang.db.repository.FridgeRepository;
import com.kaya.yatang.db.repository.FreezerItemRepository;
import com.kaya.yatang.db.repository.FridgeItemRepository;
import com.kaya.yatang.dto.UserDTO;
import com.kaya.yatang.db.entity.User;
import com.kaya.yatang.db.repository.UserRepository;
import com.kaya.yatang.dto.request.GuestImportRequest;
import com.kaya.yatang.dto.request.NicknameUpdateRequest;
import com.kaya.yatang.dto.request.ItemRequest;
import com.kaya.yatang.dto.request.SignupRequest;
import com.kaya.yatang.dto.response.SignupResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FridgeRepository fridgeRepository;
    private final FridgeItemRepository fridgeItemRepository;
    private final FreezerItemRepository freezerItemRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 일반 회원가입 (냉장고 자동 생성)
     */
    public SignupResponse signup(SignupRequest request) {
        // 최소한의 서버 검증만 (보안 목적)
        if (request.getPassword() == null || !request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 중복 확인 (데이터 무결성)
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 존재하는 사용자명입니다.");
        }

        // 임시 닉네임 생성
        String tempNickname = generateTempNickname();

        // 사용자 생성
        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(tempNickname);
        user.setLoginType(LoginType.NORMAL);

        User savedUser = userRepository.save(user);

        // 메인 냉장고 생성 (OneToMany 설계에 맞춤)
        Fridge mainFridge = createMainFridge(savedUser);

        return new SignupResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getUsername(),
                savedUser.getNickname(),
                mainFridge.getId(),
                "회원가입이 완료되었습니다.",
                savedUser.getCreatedAt());
    }

    /**
     * 닉네임 업데이트
     */
    public UserDTO updateNickname(Long userId, NicknameUpdateRequest request) {
        User user = userRepository.findById(Objects.requireNonNull(userId, "userId"))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 닉네임 중복 확인 (데이터 무결성)
        if (userRepository.existsByNicknameAndIdNot(request.getNickname(), userId)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        user.setNickname(request.getNickname());
        User updatedUser = userRepository.save(user);

        return new UserDTO(updatedUser);
    }

    /**
     * 사용자 정보 조회 (DTO 반환)
     */
    @Transactional(readOnly = true)
    public UserDTO getUserProfile(Long userId) {
        User user = userRepository.findById(Objects.requireNonNull(userId, "userId"))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        return new UserDTO(user);
    }

    // 임시 닉네임 생성
    private String generateTempNickname() {
        String tempNickname;
        do {
            String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            tempNickname = "user_" + uuid;
        } while (userRepository.existsByNickname(tempNickname)); // 중복 방지
        return tempNickname;
    }

    /**
     * 닉네임 존재 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean isNicknameExists(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    /**
     * 이메일 존재 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 사용자명 존재 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean isUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * 메인 냉장고 생성 (OneToMany 설계)
     */
    private Fridge createMainFridge(User user) {
        Fridge mainFridge = new Fridge();
        mainFridge.setName("메인 냉장고");
        mainFridge.setDescription("회원가입시 자동으로 생성된 냉장고입니다.");
        mainFridge.setUser(user);
        mainFridge.setIsMain(true);

        return fridgeRepository.save(mainFridge);
    }

    /**
     * 비밀번호 변경
     */
    public void updatePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(Objects.requireNonNull(userId, "userId"))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * 게스트(로컬) 데이터를 로그인 계정으로 병합(import)
     *
     * 정책:
     * - 서버의 메인 냉장고는 유지 (import된 냉장고는 모두 isMain=false로 저장)
     * - 냉장고 이름이 중복되면 suffix를 붙여 고유하게 생성
     * - 아이템은 그대로 생성 (단, quantity/unit이 없으면 기본값 적용)
     */
    public Map<String, Object> importGuestData(Long userId, GuestImportRequest request) {
        User user = userRepository.findById(Objects.requireNonNull(userId, "userId"))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        List<Fridge> existingFridges = fridgeRepository.findByUserId(Objects.requireNonNull(userId, "userId"));

        int createdFridges = 0;
        int createdFridgeItems = 0;
        int createdFreezerItems = 0;

        if (request == null || request.getFridges() == null) {
            Map<String, Object> res = new HashMap<>();
            res.put("message", "가져올 데이터가 없습니다.");
            res.put("createdFridges", 0);
            res.put("createdFridgeItems", 0);
            res.put("createdFreezerItems", 0);
            return res;
        }

        for (GuestImportRequest.GuestImportedFridge imported : request.getFridges()) {
            if (imported == null) continue;

            String baseName = imported.getName() != null && !imported.getName().trim().isEmpty()
                    ? imported.getName().trim()
                    : "가져온 냉장고";
            String uniqueName = makeUniqueFridgeName(existingFridges, baseName);

            Fridge fridge = new Fridge();
            fridge.setName(uniqueName);
            fridge.setDescription(imported.getDescription() == null ? "" : imported.getDescription());
            fridge.setUser(user);
            fridge.setIsMain(false);
            fridge.setDeleted(false);

            Fridge savedFridge = fridgeRepository.save(fridge);
            existingFridges.add(savedFridge);
            createdFridges++;

            if (imported.getFridgeItems() != null) {
                for (ItemRequest itemReq : imported.getFridgeItems()) {
                    if (itemReq == null) continue;
                    FridgeItem item = FridgeItem.builder()
                            .fridge(savedFridge)
                            .name(itemReq.getName())
                            .quantity(itemReq.getQuantity() != null ? itemReq.getQuantity() : 1)
                            .unit(itemReq.getUnit() != null ? itemReq.getUnit() : "개")
                            .expirationDate(itemReq.getExpirationDate())
                            .manufactureDate(itemReq.getManufactureDate())
                            .memo(itemReq.getMemo())
                            .build();
                    fridgeItemRepository.save(Objects.requireNonNull(item, "fridgeItem"));
                    createdFridgeItems++;
                }
            }

            if (imported.getFreezerItems() != null) {
                for (ItemRequest itemReq : imported.getFreezerItems()) {
                    if (itemReq == null) continue;
                    FreezerItem item = FreezerItem.builder()
                            .fridge(savedFridge)
                            .name(itemReq.getName())
                            .quantity(itemReq.getQuantity() != null ? itemReq.getQuantity() : 1)
                            .unit(itemReq.getUnit() != null ? itemReq.getUnit() : "개")
                            .expirationDate(itemReq.getExpirationDate())
                            .manufactureDate(itemReq.getManufactureDate())
                            .freezeDate(itemReq.getFreezeDate())
                            .memo(itemReq.getMemo())
                            .build();
                    freezerItemRepository.save(Objects.requireNonNull(item, "freezerItem"));
                    createdFreezerItems++;
                }
            }
        }

        Map<String, Object> res = new HashMap<>();
        res.put("message", "게스트 기록이 병합되었습니다.");
        res.put("createdFridges", createdFridges);
        res.put("createdFridgeItems", createdFridgeItems);
        res.put("createdFreezerItems", createdFreezerItems);
        return res;
    }

    private String makeUniqueFridgeName(List<Fridge> existingFridges, String baseName) {
        String candidate = baseName;
        int n = 1;
        while (fridgeNameExists(existingFridges, candidate)) {
            n++;
            candidate = baseName + " (가져옴 " + n + ")";
        }
        return candidate;
    }

    private boolean fridgeNameExists(List<Fridge> existingFridges, String name) {
        if (existingFridges == null) return false;
        for (Fridge f : existingFridges) {
            if (f != null && f.getName() != null && f.getName().equals(name)) return true;
        }
        return false;
    }
}
