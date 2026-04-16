package com.kaya.yatang.controller;

import com.kaya.yatang.service.UserIngredientImageService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/ingredient-images")
@RequiredArgsConstructor
public class PublicIngredientImageController {

    private final UserIngredientImageService userIngredientImageService;

    @GetMapping("/{publicId}")
    public ResponseEntity<Resource> get(@PathVariable String publicId) throws Exception {
        Optional<String> ct = userIngredientImageService.getContentType(publicId);
        Optional<Resource> resource = userIngredientImageService.loadImageFile(publicId);
        if (resource.isEmpty() || ct.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        MediaType mediaType = MediaType.parseMediaType(ct.get());
        return ResponseEntity.ok()
                .contentType(mediaType)
                .cacheControl(CacheControl.maxAge(java.time.Duration.ofDays(7)))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(resource.get());
    }
}
