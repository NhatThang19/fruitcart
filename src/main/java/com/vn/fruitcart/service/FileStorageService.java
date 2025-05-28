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

    /**
     * Constructor của FileStorageService.
     * Khởi tạo vị trí lưu trữ file gốc từ đường dẫn được cung cấp trong
     * application.properties.
     * Đồng thời tạo thư mục lưu trữ nếu nó chưa tồn tại.
     *
     * @param uploadDir Đường dẫn đến thư mục gốc để lưu trữ file, được inject từ
     *                  application.properties (ví dụ:
     *                  'D:/DaiHoc/DATN/fruitcart/upload/images').
     * @throws RuntimeException nếu không thể tạo thư mục lưu trữ.
     */
    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        // Chuyển đổi chuỗi đường dẫn thành đối tượng Path, chuẩn hóa và lấy đường dẫn
        // tuyệt đối.
        this.rootFileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            // Tạo thư mục gốc nếu nó chưa tồn
            Files.createDirectories(this.rootFileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException(
                    "Không thể tạo thư mục để lưu trữ file đã upload. Đường dẫn: " + this.rootFileStorageLocation, ex);
        }
    }

    /**
     * Lưu file được upload lên vào thư mục con được chỉ định bên trong thư mục gốc.
     *
     * @param file             MultipartFile được upload.
     * @param subDirectoryName Tên thư mục con (ví dụ: "user_avatars",
     *                         "product_images"). Nếu rỗng hoặc null, file sẽ được
     *                         lưu trực tiếp trong thư mục gốc.
     * @return Đường dẫn tương đối của file đã lưu (bao gồm cả thư mục con nếu có),
     *         ví dụ: "user_avatars/filename.jpg".
     * @throws RuntimeException nếu tên file không hợp lệ hoặc có lỗi trong quá
     *                          trình lưu file.
     */
    public String storeFile(MultipartFile file, String subDirectoryName) {
        // Kiểm tra file có rỗng không
        if (file.isEmpty()) {
            throw new RuntimeException("Không thể lưu file rỗng.");
        }

        // Lấy tên file gốc và làm sạch nó
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Kiểm tra ký tự không hợp lệ trong tên file
            if (originalFileName.contains("..")) {
                throw new RuntimeException("Tên file chứa chuỗi không hợp lệ: " + originalFileName);
            }

            // Tạo phần mở rộng file (ví dụ: ".jpg")
            String fileExtension = "";
            int lastDotIndex = originalFileName.lastIndexOf('.');
            if (lastDotIndex > 0 && lastDotIndex < originalFileName.length() - 1) {
                fileExtension = originalFileName.substring(lastDotIndex);
            }

            // Tạo tên file mới duy nhất bằng UUID để tránh trùng lặp
            String storedFileName = UUID.randomUUID().toString() + fileExtension;

            // Xác định thư mục đích (thư mục gốc hoặc thư mục con)
            Path targetDirectory = this.rootFileStorageLocation;
            String relativePathPrefix = ""; // Dùng để xây dựng đường dẫn tương đối trả về

            if (subDirectoryName != null && !subDirectoryName.trim().isEmpty()) {
                targetDirectory = this.rootFileStorageLocation.resolve(subDirectoryName.trim());
                relativePathPrefix = subDirectoryName.trim() + "/";
                // Tạo thư mục con nếu chưa tồn tại
                Files.createDirectories(targetDirectory);
            }

            // Đường dẫn tuyệt đối đến file đích
            Path targetLocation = targetDirectory.resolve(storedFileName);

            // Sao chép file vào vị trí đích
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            // Trả về đường dẫn tương đối để lưu vào DB
            return "/storage/" + relativePathPrefix + storedFileName;

        } catch (IOException ex) {
            throw new RuntimeException("Không thể lưu file " + originalFileName + ". Vui lòng thử lại!", ex);
        }
    }

    /**
     * Xóa file dựa trên đường dẫn tương đối của nó so với thư mục lưu trữ gốc.
     *
     * @param relativeFilePath Đường dẫn tương đối của file cần xóa (ví dụ:
     *                         "user_avatars/filename.jpg" hoặc "filename.jpg").
     */
    public void deleteFile(String relativeFilePath) {
        if (relativeFilePath == null || relativeFilePath.trim().isEmpty()) {
            // Không làm gì nếu đường dẫn rỗng hoặc null
            return;
        }

        try {
            Path fileToDelete = this.rootFileStorageLocation.resolve(relativeFilePath).normalize();

            // Thêm một bước kiểm tra an toàn: đảm bảo file cần xóa nằm trong thư mục lưu
            // trữ gốc
            if (!fileToDelete.startsWith(this.rootFileStorageLocation)) {
                System.err.println("Cảnh báo: Cố gắng xóa file bên ngoài thư mục upload: " + relativeFilePath);
                return;
            }

            Files.deleteIfExists(fileToDelete);
        } catch (IOException ex) {
            // Log lỗi thay vì throw exception để không làm gián đoạn luồng chính nếu việc
            // xóa file không quá quan trọng
            System.err.println("Không thể xóa file: " + relativeFilePath + ". Lỗi: " + ex.getMessage());
        }
    }
}