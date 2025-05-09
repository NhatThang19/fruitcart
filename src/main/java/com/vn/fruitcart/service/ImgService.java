package com.vn.fruitcart.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class ImgService {
    @Value("${upload.file.base.path}") // Giờ là: D:/DaiHoc/DATN/fruitcart/upload/images/
    private String uploadBasePath;

    public String storeFile(MultipartFile file, String subFolder) {
        if (file == null || file.isEmpty()) {
            return "default_avatar.jpg";
        }

        try {
            Path uploadPath = getNormalizedPath(subFolder);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFileName = file.getOriginalFilename();
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            Path filePath = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath);

            return uniqueFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store file: " + ex.getMessage(), ex);
        }
    }

    private Path getNormalizedPath(String subFolder) {
        // 1. Loại bỏ "file://" nếu có
        String normalizedPath = uploadBasePath.replaceFirst("^file:/+", "");

        // 2. Thay thế tất cả dấu \ thành /
        normalizedPath = normalizedPath.replace('\\', '/');

        // 3. Kết hợp với subFolder và chuẩn hóa lại
        return Paths.get(normalizedPath, subFolder.replace('\\', '/')).normalize();
    }
}
