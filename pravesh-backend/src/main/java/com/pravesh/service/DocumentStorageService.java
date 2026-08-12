package com.pravesh.service;

import com.pravesh.exception.InvalidStateException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentStorageService {

    private final Path baseDir;

    private static final long MAX_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    private static final List<String> ALLOWED_EXTENSIONS =
            List.of("pdf", "jpg", "jpeg", "png");

    public DocumentStorageService(@Value("${pravesh.upload.onboarding-dir}") String baseDir) {
        this.baseDir = Path.of(baseDir);
    }

    public String store(Long userId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidStateException("Uploaded file is empty");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new InvalidStateException("File must be under 5MB");
        }

        String originalName = file.getOriginalFilename();
        String extension = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase()
                : "";

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidStateException(
                    "Only PDF, JPG, and PNG files are allowed");
        }

        try {
            Path userDir = baseDir.resolve(String.valueOf(userId));
            Files.createDirectories(userDir);

            String filename = UUID.randomUUID() + "." + extension;
            Path destination = userDir.resolve(filename);

            file.transferTo(destination);

            return destination.toString();

        } catch (IOException e) {
            throw new RuntimeException("Failed to store uploaded document", e);
        }
    }
}