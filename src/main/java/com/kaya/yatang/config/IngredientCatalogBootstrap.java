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
import java.util.Set;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * 기본 식재료 카탈로그(시스템 공용, user_id NULL)를 최초 1회 시드합니다.
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
        List<String> names = loadLinesFromResource();
        if (names.isEmpty()) {
            names = fallbackNames();
        }
        Set<String> seen = new LinkedHashSet<>();
        List<IngredientCatalogEntry> batch = new ArrayList<>();
        for (String n : names) {
            if (n == null || n.isBlank()) continue;
            String name = n.trim();
            if (!seen.add(name)) continue;
            batch.add(IngredientCatalogEntry.builder()
                    .name(name)
                    .defaultUnit(defaultUnitFor(name))
                    .user(null)
                    .build());
        }
        if (!batch.isEmpty()) {
            catalogRepository.saveAll(batch);
        }
    }

    private static String defaultUnitFor(String name) {
        if (name.endsWith("ml") || name.contains("mL")) return "ml";
        if (name.endsWith("L") && name.length() <= 3) return "L";
        if (name.endsWith("g") && name.length() <= 4) return "g";
        return "개";
    }

    private List<String> loadLinesFromResource() {
        List<String> out = new ArrayList<>();
        try {
            ClassPathResource res = new ClassPathResource("ingredient-catalog-default.txt");
            if (!res.exists()) return out;
            try (InputStream in = res.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    out.add(line);
                }
            }
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
        return out;
    }

    /** 리소스가 없을 때 최소 목록 (간장·고추장·새우 포함) */
    private List<String> fallbackNames() {
        List<String> list = new ArrayList<>();
        list.add("간장");
        list.add("고추장");
        list.add("새우");
        for (int i = 1; i <= 197; i++) {
            list.add("기본식재료_" + String.format(Locale.ROOT, "%03d", i));
        }
        return list;
    }
}
