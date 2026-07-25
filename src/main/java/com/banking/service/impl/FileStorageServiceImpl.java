package com.banking.service.impl;

import com.banking.exception.ValidationException;
import com.banking.service.FileStorageService;
import com.banking.util.BankingConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Implementation of {@link FileStorageService} storing files to the local filesystem.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${banking.file.upload-dir:./uploads}")
    private String baseUploadDir;

    @Override
    public String storeFile(MultipartFile file, String subfolder) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("File is empty");
        }
        if (file.getSize() > BankingConstants.MAX_FILE_SIZE_BYTES) {
            throw new ValidationException("File size exceeds the 5 MB limit");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new ValidationException("Invalid file name");
        }

        String extension = getExtension(originalFilename).toLowerCase();
        if (!BankingConstants.ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new ValidationException("Only image files are allowed (.jpg, .jpeg, .png, .gif)");
        }

        String uniqueFilename = UUID.randomUUID() + extension;
        Path targetDir = Paths.get(baseUploadDir, subfolder);

        try {
            Files.createDirectories(targetDir);
            Path targetPath = targetDir.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            String urlPath = "/uploads/" + subfolder + "/" + uniqueFilename;
            log.info("File stored: {}", urlPath);
            return urlPath;
        } catch (IOException e) {
            log.error("Failed to store file: {}", e.getMessage(), e);
            throw new ValidationException("Failed to store file: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return;
        }
        try {
            Path path = Paths.get(baseUploadDir,
                    filePath.replace("/uploads/", "").replace("/", java.io.File.separator));
            Files.deleteIfExists(path);
            log.info("File deleted: {}", filePath);
        } catch (IOException e) {
            log.warn("Could not delete file {}: {}", filePath, e.getMessage());
        }
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex >= 0 ? filename.substring(dotIndex) : "";
    }
}
