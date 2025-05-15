package com.vn.fruitcart.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {
    @Value("${upload.file.base.path}")
    private String uploadBasePath;

    public String storeFile(MultipartFile file, String subFolder) {
        if (file == null || file.isEmpty()) {
            return "default_avatar.jpg";
        }

        try {
            Path uploadPathDir = getNormalizedPath(subFolder);
            if (!Files.exists(uploadPathDir)) {
                Files.createDirectories(uploadPathDir);
            }

            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            Path filePath = uploadPathDir.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return uniqueFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store file: " + (file != null ? file.getOriginalFilename() : "null")
                    + ". Error: " + ex.getMessage(), ex);
        }
    }

    public List<String> storeMultipleFiles(List<MultipartFile> files, String subFolder) {
        if (files == null || files.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> storedFileNames = new ArrayList<>();
        Path uploadPathDir = getNormalizedPath(subFolder);

        try {
            if (!Files.exists(uploadPathDir)) {
                Files.createDirectories(uploadPathDir);
            }
        } catch (IOException ex) {
            throw new RuntimeException(
                    "Failed to create upload directory: " + uploadPathDir + ". Error: " + ex.getMessage(), ex);
        }

        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                try {
                    String originalFileName = file.getOriginalFilename();
                    String fileExtension = "";
                    if (originalFileName != null && originalFileName.contains(".")) {
                        fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
                    }
                    String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

                    Path filePath = uploadPathDir.resolve(uniqueFileName);
                    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                    storedFileNames.add(uniqueFileName);
                } catch (IOException ex) {
                    System.err.println("Failed to store file: " + file.getOriginalFilename() + ". Error: "
                            + ex.getMessage() + ". Skipping this file.");
                }
            }
        }
        return storedFileNames;
    }

    private Path getNormalizedPath(String subFolder) {
        String normalizedBasePath = uploadBasePath.replaceFirst("^file:/+", "");
        normalizedBasePath = normalizedBasePath.replace('\\', '/');
        String normalizedSubFolder = (subFolder == null ? "" : subFolder.replace('\\', '/'));

        if (normalizedBasePath.endsWith("/") && normalizedSubFolder.startsWith("/")) {
            normalizedSubFolder = normalizedSubFolder.substring(1);
        } else if (!normalizedBasePath.endsWith("/") && !normalizedSubFolder.startsWith("/")
                && !normalizedSubFolder.isEmpty()) {
            normalizedSubFolder = "/" + normalizedSubFolder;
        }

        return Paths.get(normalizedBasePath + normalizedSubFolder).normalize();
    }
}