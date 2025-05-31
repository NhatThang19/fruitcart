package com.vn.fruitcart.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import com.vn.fruitcart.entity.Category;
import com.vn.fruitcart.entity.Origin;
import com.vn.fruitcart.entity.Product;
import com.vn.fruitcart.entity.ProductImage;
import com.vn.fruitcart.entity.ProductVariant;
import com.vn.fruitcart.entity.dto.request.ProductCreateReq;
import com.vn.fruitcart.entity.dto.request.ProductVariantReq;
import com.vn.fruitcart.exception.ResourceNotFoundException;
import com.vn.fruitcart.repository.CategoryRepository;
import com.vn.fruitcart.repository.OriginRepository;
import com.vn.fruitcart.repository.ProductRepository;
import com.vn.fruitcart.util.StringUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OriginRepository originRepository;
    private final FileStorageService fileStorageService;

    public Product createProduct(ProductCreateReq productDTO, List<MultipartFile> images, List<Boolean> isMainImages)
            throws Exception {
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> {
                    return new ResourceNotFoundException(
                            "Không tìm thấy danh mục với ID: " + productDTO.getCategoryId());
                });

        Origin origin = originRepository.findById(productDTO.getOriginId())
                .orElseThrow(() -> {
                    return new ResourceNotFoundException(
                            "Không tìm thấy nguồn gốc với ID: " + productDTO.getOriginId());
                });

        Product product = new Product();
        product.setName(productDTO.getName());
        product.setSlug(StringUtil.toSlug(productDTO.getName()));
        product.setDescription(productDTO.getDescription());
        product.setBasePrice(productDTO.getBasePrice());
        product.setNew(productDTO.isNew());
        product.setStatus(productDTO.isStatus());

        product.setCategory(category);
        product.setOrigin(origin);

        if (!CollectionUtils.isEmpty(productDTO.getVariants())) {
            for (ProductVariantReq variantDTO : productDTO.getVariants()) {
                ProductVariant variant = new ProductVariant();
                variant.setSku(variantDTO.getSku());
                variant.setPrice(variantDTO.getPrice());
                variant.setAttribute(variantDTO.getAttribute());
                product.addVariant(variant);
            }
        } else {
            ProductVariant defaultVariant = new ProductVariant();
            defaultVariant.setSku(StringUtil.toSlug(product.getName()) + "-default");
            defaultVariant.setPrice(product.getBasePrice());
            defaultVariant.setAttribute("Mặc định");
            product.addVariant(defaultVariant);
        }

        handleProductImagesUpload(product, images, isMainImages, true, null, null);

        Product savedProduct = productRepository.save(product);
        return savedProduct;
    }

    private void handleProductImagesUpload(Product product, List<MultipartFile> images, List<Boolean> isMainFlags,
            boolean isCreateMode, Long mainImageIdForUpdate, Integer newMainImageIndexForUpdate) throws IOException {
        if (CollectionUtils.isEmpty(images)) {
            if (!isCreateMode && mainImageIdForUpdate != null && newMainImageIndexForUpdate == null) {
                setMainImageForExisting(product, mainImageIdForUpdate);
            }
            return;
        }

        List<ProductImage> newlyUploadedImages = new ArrayList<>();
        boolean newMainImageSetByFlagOrIndex = false;

        for (int i = 0; i < images.size(); i++) {
            MultipartFile imageFile = images.get(i);
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = fileStorageService.storeFile(imageFile, "product_images");
                ProductImage productImage = new ProductImage();
                productImage.setImageUrl(imageUrl);
                newlyUploadedImages.add(productImage);

                if (isCreateMode) {
                    if (isMainFlags != null && i < isMainFlags.size() && isMainFlags.get(i)) {
                        productImage.setMain(true);
                        newMainImageSetByFlagOrIndex = true;
                    } else {
                        productImage.setMain(false);
                    }
                } else {
                    if (newMainImageIndexForUpdate != null && i == newMainImageIndexForUpdate) {
                        productImage.setMain(true);
                        newMainImageSetByFlagOrIndex = true;
                    } else {
                        productImage.setMain(false);
                    }
                }
            }
        }

        if (!isCreateMode) {
            if (newMainImageSetByFlagOrIndex) {
                product.getImages().forEach(img -> img.setMain(false));
            } else if (mainImageIdForUpdate != null) {
                setMainImageForExisting(product, mainImageIdForUpdate);
            }
        }

        for (ProductImage newImg : newlyUploadedImages) {
            product.addImage(newImg);
        }

        long mainImageCount = product.getImages().stream().filter(ProductImage::isMain).count();
        if (mainImageCount == 0 && !product.getImages().isEmpty()) {
            product.getImages().get(0).setMain(true);
        } else if (mainImageCount > 1) {
            boolean firstMainFound = false;
            for (ProductImage img : product.getImages()) {
                if (img.isMain()) {
                    if (firstMainFound) {
                        img.setMain(false);
                    } else {
                        firstMainFound = true;
                    }
                }
            }
        }
    }

    private void setMainImageForExisting(Product product, Long mainImageIdToSet) {
        boolean mainSet = false;
        for (ProductImage img : product.getImages()) {
            if (img.getId() != null && img.getId().equals(mainImageIdToSet)) {
                img.setMain(true);
                mainSet = true;
            } else {
                img.setMain(false);
            }
        }
        if (!mainSet && !product.getImages().isEmpty()
                && product.getImages().stream().noneMatch(ProductImage::isMain)) {
            product.getImages().get(0).setMain(true);
        }
    }
}
