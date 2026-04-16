package com.kaya.yatang.service;

import com.kaya.yatang.config.IngredientImageProperties;
import com.kaya.yatang.db.entity.User;
import com.kaya.yatang.db.entity.UserIngredientImage;
import com.kaya.yatang.db.repository.IngredientCatalogEntryRepository;
import com.kaya.yatang.db.repository.UserIngredientImageRepository;
import com.kaya.yatang.db.repository.UserRepository;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserIngredientImageService {

    private static final Set<String> ALLOWED_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final long MAX_BYTES = 2 * 1024 * 1024;

    private final UserIngredientImageRepository imageRepository;
    private final UserRepository userRepository;
    private final IngredientCatalogEntryRepository catalogRepository;
    private final IngredientImageProperties properties;

    @Transactional(readOnly = true)
    public Map<String, String> getPublicIdByIngredientNameLower(Long userId) {
        Map<String, String> map = new HashMap<>();
        if (userId == null) {
            return map;
        }
        Set<String> customNamesLower = catalogRepository.findByUserIdOrderByNameAsc(userId).stream()
                .map(e -> e.getName().trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        for (UserIngredientImage img : imageRepository.findByUserId(userId)) {
            String key = img.getIngredientName().trim().toLowerCase(Locale.ROOT);
            if (customNamesLower.contains(key)) {
                map.put(key, img.getPublicId());
            }
        }
        return map;
    }

    @Transactional(readOnly = true)
    public Optional<Resource> loadImageFile(String publicId) throws IOException {
        Optional<UserIngredientImage> row = imageRepository.findByPublicId(publicId);
        if (row.isEmpty()) {
            return Optional.empty();
        }
        Path path = resolveStoredPath(row.get());
        if (!Files.isRegularFile(path)) {
            return Optional.empty();
        }
        return Optional.of(new UrlResource(path.toUri()));
    }

    @Transactional(readOnly = true)
    public Optional<String> getContentType(String publicId) {
        return imageRepository.findByPublicId(publicId).map(UserIngredientImage::getContentType);
    }

    @Transactional
    public UserIngredientImage upload(Long userId, String ingredientName, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일을 선택해주세요.");
        }
        String name = ingredientName == null ? "" : ingredientName.trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("재료 이름을 입력해주세요.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("이미지는 2MB 이하만 업로드할 수 있습니다.");
        }
        String ct = file.getContentType();
        if (ct == null || !ALLOWED_TYPES.contains(ct.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("JPEG, PNG, WebP, GIF 이미지만 업로드할 수 있습니다.");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (!catalogRepository.existsByUserIdAndNameIgnoreCase(userId, name)) {
            throw new IllegalArgumentException("직접 추가한 재료에만 사진을 등록할 수 있습니다.");
        }

        String ext = extensionFromContentType(ct);
        String publicId = UUID.randomUUID().toString();

        Path dir = resolveStorageDir();
        Files.createDirectories(dir);
        Path target = dir.resolve(publicId + "." + ext);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        Optional<UserIngredientImage> existing = imageRepository.findByUserIdAndIngredientNameIgnoreCase(userId, name);
        if (existing.isPresent()) {
            UserIngredientImage old = existing.get();
            deleteStoredFileIfExists(old);
            old.setPublicId(publicId);
            old.setContentType(ct);
            old.setFileExtension(ext);
            return imageRepository.save(old);
        }

        UserIngredientImage entity = UserIngredientImage.builder()
                .user(user)
                .ingredientName(name)
                .publicId(publicId)
                .contentType(ct)
                .fileExtension(ext)
                .build();
        return imageRepository.save(entity);
    }

    @Transactional
    public void delete(Long userId, String ingredientName) {
        String name = ingredientName == null ? "" : ingredientName.trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("재료 이름을 입력해주세요.");
        }
        Optional<UserIngredientImage> row = imageRepository.findByUserIdAndIngredientNameIgnoreCase(userId, name);
        if (row.isEmpty()) {
            return;
        }
        UserIngredientImage img = row.get();
        deleteStoredFileIfExists(img);
        imageRepository.delete(img);
    }

    private void deleteStoredFileIfExists(UserIngredientImage img) {
        try {
            Path p = resolveStoredPath(img);
            Files.deleteIfExists(p);
        } catch (IOException ignored) {
            // best effort
        }
    }

    private Path resolveStorageDir() {
        Path p = Paths.get(properties.getStorageDir());
        if (!p.isAbsolute()) {
            p = Paths.get(System.getProperty("user.dir")).resolve(p);
        }
        return p;
    }

    private Path resolveStoredPath(UserIngredientImage img) {
        return resolveStorageDir().resolve(img.getPublicId() + "." + img.getFileExtension());
    }

    private static String extensionFromContentType(String contentType) {
        String ct = contentType.toLowerCase(Locale.ROOT);
        if (ct.contains("jpeg") || ct.contains("jpg")) {
            return "jpg";
        }
        if (ct.contains("png")) {
            return "png";
        }
        if (ct.contains("webp")) {
            return "webp";
        }
        if (ct.contains("gif")) {
            return "gif";
        }
        return "bin";
    }
}
