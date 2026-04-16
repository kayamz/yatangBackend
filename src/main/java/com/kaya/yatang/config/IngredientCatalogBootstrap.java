package com.kaya.yatang.config;

import com.kaya.yatang.db.entity.IngredientCatalogEntry;
import com.kaya.yatang.db.repository.IngredientCatalogEntryRepository;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * 기본 식재료 카탈로그(시스템 공용, user_id NULL)를 최초 1회 시드합니다.
 * 리소스 형식: category|name|defaultUnit [|iconImageFile] (4번째 칸 생략 가능, 영문 파일명 예: onion.png)
 */
@Component
@Order(50)
@RequiredArgsConstructor
public class IngredientCatalogBootstrap implements CommandLineRunner {

    private final IngredientCatalogEntryRepository catalogRepository;

    @Override
    public void run(String... args) {
        if (catalogRepository.countByUserIsNull() > 0) {
            return;
        }
        List<IngredientCatalogEntry> batch = loadEntriesFromResource();
        if (batch.isEmpty()) {
            batch = fallbackEntries();
        }
        if (!batch.isEmpty()) {
            catalogRepository.saveAll(batch);
        }
    }

    private List<IngredientCatalogEntry> loadEntriesFromResource() {
        List<IngredientCatalogEntry> out = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        try {
            ClassPathResource res = new ClassPathResource("ingredient-catalog-default.txt");
            if (!res.exists()) {
                return out;
            }
            try (InputStream in = res.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.isBlank() || line.startsWith("#")) {
                        continue;
                    }
                    String[] p = line.split("\\|", -1);
                    if (p.length >= 3) {
                        String category = p[0].trim();
                        String name = p[1].trim();
                        String unit = p[2].trim();
                        String iconFile = p.length >= 4 ? p[3].trim() : "";
                        if (name.isEmpty() || unit.isEmpty()) {
                            continue;
                        }
                        if (!seen.add(name.toLowerCase(Locale.ROOT))) {
                            continue;
                        }
                        out.add(IngredientCatalogEntry.builder()
                                .category(category.isEmpty() ? null : category)
                                .name(name)
                                .defaultUnit(unit)
                                .iconImageFile(iconFile.isEmpty() ? null : iconFile)
                                .user(null)
                                .build());
                    } else {
                        String name = line.trim();
                        if (name.isEmpty() || !seen.add(name.toLowerCase(Locale.ROOT))) {
                            continue;
                        }
                        out.add(IngredientCatalogEntry.builder()
                                .name(name)
                                .defaultUnit(defaultUnitFor(name))
                                .user(null)
                                .build());
                    }
                }
            }
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
        return out;
    }

    private static String defaultUnitFor(String name) {
        if (name.endsWith("ml") || name.contains("mL")) {
            return "ml";
        }
        if (name.endsWith("L") && name.length() <= 3) {
            return "L";
        }
        if (name.endsWith("g") && name.length() <= 4) {
            return "g";
        }
        return "개";
    }

    /** 리소스가 없을 때 최소 목록 (김치·돼지고기·양파 포함) */
    private List<IngredientCatalogEntry> fallbackEntries() {
        List<String> list = new ArrayList<>();
        list.add("김치");
        list.add("돼지고기");
        list.add("양파");
        for (int i = 1; i <= 197; i++) {
            list.add("기본식재료_" + String.format(Locale.ROOT, "%03d", i));
        }
        List<IngredientCatalogEntry> batch = new ArrayList<>();
        for (String n : list) {
            batch.add(IngredientCatalogEntry.builder()
                    .name(n)
                    .defaultUnit(defaultUnitFor(n))
                    .user(null)
                    .build());
        }
        return batch;
    }
}
