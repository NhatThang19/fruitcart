package com.vn.fruitcart.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.vn.fruitcart.entity.Product;
import com.vn.fruitcart.entity.ProductImage;
import com.vn.fruitcart.repository.ProductImageRepository;

@Service
public class ProductImageService {
    private final ProductImageRepository productImageRepository;
    private final FileStorageService fileStorageService;

    public ProductImageService(ProductImageRepository productImageRepository, FileStorageService fileStorageService) {
        this.productImageRepository = productImageRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<ProductImage> saveProductImages(Product product,
            MultipartFile mainImage,
            MultipartFile[] thumbnailFiles) {
        List<ProductImage> savedImages = new ArrayList<>();

        // Xử lý ảnh chính
        if (mainImage != null && !mainImage.isEmpty()) {
            String imageUrl = fileStorageService.storeFile(mainImage, "products");
            ProductImage mainProductImage = ProductImage.builder()
                    .product(product)
                    .imageUrl(imageUrl)
                    .isMain(true)
                    .build();
            savedImages.add(productImageRepository.save(mainProductImage));
        }

        // Xử lý các ảnh phụ
        if (thumbnailFiles != null) {
            for (MultipartFile image : thumbnailFiles) {
                if (image != null && !image.isEmpty()) {
                    String imageUrl = fileStorageService.storeFile(image, "products");
                    ProductImage additionalImage = ProductImage.builder()
                            .product(product)
                            .imageUrl(imageUrl)
                            .isMain(false)
                            .build();
                    savedImages.add(productImageRepository.save(additionalImage));
                }
            }
        }

        return savedImages;
    }
}
