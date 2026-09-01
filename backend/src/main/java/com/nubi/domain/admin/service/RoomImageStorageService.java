package com.nubi.domain.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Set;
import java.util.UUID;

// 로컬 디스크에 파일 저장
@Component
public class RoomImageStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public String store(Long roomId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "empty file");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unsupported image type: " + contentType);
        }

        try {
            Path roomDir = roomDir(roomId);
            Files.createDirectories(roomDir);

            String filename = UUID.randomUUID() + extractExtension(file.getOriginalFilename(), contentType);
            Path target = roomDir.resolve(filename);
            file.transferTo(target);

            return "/uploads/rooms/" + roomId + "/" + filename;
        } catch (IOException e) {
            throw new UncheckedIOException("failed to store image", e);
        }
    }

    public void delete(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith("/uploads/")) {
            return;
        }
        Path path = baseDir().resolve(imageUrl.substring("/uploads/".length()));
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {}
    }

    // 방이 삭제될 때 해당 방의 사진 디렉터리를 정리
    public void deleteRoomDirectory(Long roomId) {
        Path roomDir = roomDir(roomId);
        if (!Files.exists(roomDir)) {
            return;
        }
        try (var paths = Files.walk(roomDir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {}
            });
        } catch (IOException ignored) {}
    }

    private Path roomDir(Long roomId) {
        return baseDir().resolve(Paths.get("rooms", String.valueOf(roomId)));
    }

    private Path baseDir() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    private String extractExtension(String originalFilename, String contentType) {
        if (originalFilename != null && originalFilename.contains(".")) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf('.'));
            if (ext.length() <= 10) {
                return ext.toLowerCase();
            }
        }
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> "";
        };
    }
}
