package com.kaya.yatang.config;

import com.kaya.yatang.db.entity.IngredientCatalogEntry;
import com.kaya.yatang.db.repository.IngredientCatalogEntryRepository;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * classpath의 ingredient-catalog-extra.txt(카테고리|이름|단위[|iconImageFile])에 있는 항목을 시스템 카탈로그에 없으면 삽입합니다.
 */
@Component
@Order(55)
@RequiredArgsConstructor
public class IngredientCatalogExtraUpsertRunner implements CommandLineRunner {

    private final IngredientCatalogEntryRepository catalogRepository;

    @Override
    public void run(String... args) {
        List<String> lines = loadLines();
        if (lines.isEmpty()) {
            return;
        }
        List<IngredientCatalogEntry> batch = new ArrayList<>();
        for (String line : lines) {
            if (line == null || line.isBlank() || line.startsWith("#")) {
                continue;
            }
            String[] p = line.split("\\|", -1);
            if (p.length < 3) {
                continue;
            }
            String category = p[0].trim();
            String name = p[1].trim();
            String unit = p[2].trim();
            String iconFile = p.length >= 4 ? p[3].trim() : "";
            if (name.isEmpty() || unit.isEmpty()) {
                continue;
            }
            if (catalogRepository.existsByUserIsNullAndNameIgnoreCase(name)) {
                continue;
            }
            batch.add(IngredientCatalogEntry.builder()
                    .category(category.isEmpty() ? null : category)
                    .name(name)
                    .defaultUnit(unit)
                    .iconImageFile(iconFile.isEmpty() ? null : iconFile)
                    .user(null)
                    .build());
        }
        if (!batch.isEmpty()) {
            catalogRepository.saveAll(batch);
        }
    }

    private List<String> loadLines() {
        List<String> out = new ArrayList<>();
        try {
            ClassPathResource res = new ClassPathResource("ingredient-catalog-extra.txt");
            if (!res.exists()) {
                return out;
            }
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
}
