package com.kaya.yatang.dto;

import com.kaya.yatang.code.LoginType;
import com.kaya.yatang.db.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;
    private String username;
    private String email;
    private String nickname;
    private LoginType loginType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private FridgeDTO mainFridge; // 메인 냉장고 정보
    private List<FridgeDTO> fridges; // 전체 냉장고 목록

    public UserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.loginType = user.getLoginType();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();



        if (user.getFridges() != null && !user.getFridges().isEmpty()) {
            this.fridges = user.getFridges().stream()
                .map(FridgeDTO::new)
                .collect(Collectors.toList());

            this.mainFridge = user.getFridges().stream()
                .filter(fridge -> "메인 냉장고".equals(fridge.getName()))
                .map(FridgeDTO::new)
                .findFirst()
                .orElse(this.fridges.get(0));
        }
    }
}