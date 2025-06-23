package com.vn.fruitcart.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootFileStorageLocation;


    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {

        this.rootFileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.rootFileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException(
                    "Không thể tạo thư mục để lưu trữ file đã upload. Đường dẫn: " + this.rootFileStorageLocation, ex);
        }
    }


    public String storeFile(MultipartFile file, String subDirectoryName) {
        if (file.isEmpty()) {
            throw new RuntimeException("Không thể lưu file rỗng.");
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            if (originalFileName.contains("..")) {
                throw new RuntimeException("Tên file chứa chuỗi không hợp lệ: " + originalFileName);
            }

            String fileExtension = "";
            int lastDotIndex = originalFileName.lastIndexOf('.');
            if (lastDotIndex > 0 && lastDotIndex < originalFileName.length() - 1) {
                fileExtension = originalFileName.substring(lastDotIndex);
            }

            String storedFileName = UUID.randomUUID().toString() + fileExtension;

            Path targetDirectory = this.rootFileStorageLocation;
            String relativePathPrefix = "";

            if (subDirectoryName != null && !subDirectoryName.trim().isEmpty()) {
                targetDirectory = this.rootFileStorageLocation.resolve(subDirectoryName.trim());
                relativePathPrefix = subDirectoryName.trim() + "/";

                Files.createDirectories(targetDirectory);
            }

            Path targetLocation = targetDirectory.resolve(storedFileName);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            return "/storage/" + relativePathPrefix + storedFileName;

        } catch (IOException ex) {
            throw new RuntimeException("Không thể lưu file " + originalFileName + ". Vui lòng thử lại!", ex);
        }
    }


    public void deleteFile(String relativeFilePath) {
        if (relativeFilePath == null || relativeFilePath.trim().isEmpty()) {
            return;
        }

        try {
            Path fileToDelete = this.rootFileStorageLocation.resolve(relativeFilePath).normalize();

            if (!fileToDelete.startsWith(this.rootFileStorageLocation)) {
                System.err.println("Cảnh báo: Cố gắng xóa file bên ngoài thư mục upload: " + relativeFilePath);
                return;
            }

            Files.deleteIfExists(fileToDelete);
        } catch (IOException ex) {

            System.err.println("Không thể xóa file: " + relativeFilePath + ". Lỗi: " + ex.getMessage());
        }
    }
}