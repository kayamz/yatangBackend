package com.kaya.yatang.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FridgeRequest {
    private String name;
    private String description;
    private Boolean isMain;
}