package com.kaya.yatang.dto.request;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GuestImportRequest {
    private List<GuestImportedFridge> fridges = new ArrayList<>();
    /** 게스트 로컬 상온보관 목록 (사용자당 하나의 공간) */
    private List<ItemRequest> pantryItems = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GuestImportedFridge {
        private String name;
        private String description;
        private Boolean isMain;
        private List<ItemRequest> fridgeItems = new ArrayList<>();
        private List<ItemRequest> freezerItems = new ArrayList<>();
    }
}

