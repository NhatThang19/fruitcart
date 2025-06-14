package com.vn.fruitcart.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.vn.fruitcart.entity.Category;
import com.vn.fruitcart.entity.Origin;
import com.vn.fruitcart.entity.Product;
import com.vn.fruitcart.entity.ProductImage;
import com.vn.fruitcart.entity.ProductVariant;
import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.entity.dto.request.ProductUpdateReq;
import com.vn.fruitcart.entity.dto.request.ProductVariantUpdateReq;
import com.vn.fruitcart.entity.dto.request.product.ProductCreateReq;
import com.vn.fruitcart.entity.dto.request.product.ProductSearchCriteriaReq;
import com.vn.fruitcart.entity.dto.request.product.ProductVariantReq;
import com.vn.fruitcart.exception.ResourceNotFoundException;
import com.vn.fruitcart.repository.CategoryRepository;
import com.vn.fruitcart.repository.OriginRepository;
import com.vn.fruitcart.repository.ProductRepository;
import com.vn.fruitcart.service.specification.UserSpecification;
import com.vn.fruitcart.util.FruitCartUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OriginRepository originRepository;
    private final FileStorageService fileStorageService;

    @Transactional
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
        product.setSlug(FruitCartUtils.toSlug(productDTO.getName()));
        product.setShortDescription(FruitCartUtils.toSlug(productDTO.getName()));
        product.setDescription(productDTO.getDescription());
        product.setBasePrice(productDTO.getBasePrice());
        product.setNew(productDTO.isNew());
        product.setStatus(productDTO.isStatus());

        product.setCategory(category);
        product.setOrigin(origin);

        if (!CollectionUtils.isEmpty(productDTO.getVariants())) {
            for (ProductVariantReq variantDTO : productDTO.getVariants()) {
                ProductVariant variant = new ProductVariant();
                variant.setPrice(variantDTO.getPrice());
                variant.setAttribute(variantDTO.getAttribute());
                product.addVariant(variant);
            }
        } else {
            ProductVariant defaultVariant = new ProductVariant();
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

    public Product getProductById(Long id) throws ResourceNotFoundException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    return new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id);
                });
        product.getVariants().size();
        product.getImages().size();
        return product;
    }

    @Transactional
    public Product updateProduct(Long productId, ProductUpdateReq productDTO,
            List<MultipartFile> newImages,
            List<Long> imageIdsToDelete) throws Exception {

        if (!Objects.equals(productId, productDTO.getId())) {
            throw new IllegalArgumentException("ID sản phẩm không khớp để cập nhật.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    return new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + productId);
                });

        product.setName(productDTO.getName());
        if (!Objects.equals(product.getSlug(), FruitCartUtils.toSlug(productDTO.getName()))) {
            product.setSlug(FruitCartUtils.toSlug(productDTO.getName()));
        }
        product.setDescription(productDTO.getDescription());
        product.setBasePrice(productDTO.getPrice());
        product.setNew(productDTO.isNew());
        product.setStatus(productDTO.isStatus());

        if (!Objects.equals(product.getCategory().getId(), productDTO.getCategoryId())) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy danh mục với ID: " + productDTO.getCategoryId()));
            product.setCategory(category);
        }

        if (!Objects.equals(product.getOrigin().getId(), productDTO.getOriginId())) {
            Origin origin = originRepository.findById(productDTO.getOriginId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy nguồn gốc với ID: " + productDTO.getOriginId()));
            product.setOrigin(origin);
        }

        updateProductVariants(product, productDTO.getVariants());

        if (!CollectionUtils.isEmpty(imageIdsToDelete)) {
            deleteExistingImages(product, imageIdsToDelete);
        }

        handleProductImagesUpload(product, newImages, null, false, productDTO.getMainImageId(),
                productDTO.getNewMainImageIndex());

        if (!product.getImages().isEmpty() && product.getImages().stream().noneMatch(ProductImage::isMain)) {
            product.getImages().get(0).setMain(true);
        }

        Product updatedProduct = productRepository.save(product);
        return updatedProduct;
    }

    @Transactional
    private void updateProductVariants(Product product, List<ProductVariantUpdateReq> variantDTOs) {
        if (variantDTOs == null) {
            // Nếu không có DTO nào được gửi lên, hãy xóa tất cả các biến thể hiện có.
            product.getVariants().clear();
            return;
        }

        // Tạo một map các biến thể hiện có theo ID để tra cứu nhanh.
        // Dùng Collectors.toMap để xử lý hiệu quả.
        Map<Long, ProductVariant> existingVariantsMap = product.getVariants().stream()
                .collect(Collectors.toMap(ProductVariant::getId, variant -> variant));

        // Danh sách các biến thể sẽ được giữ lại hoặc thêm mới.
        List<ProductVariant> finalVariants = new ArrayList<>();

        for (ProductVariantUpdateReq dto : variantDTOs) {
            ProductVariant variant;
            if (dto.getId() != null && dto.getId() != 0) {
                // Đây là một biến thể hiện có cần cập nhật.
                variant = existingVariantsMap.get(dto.getId());
                if (variant == null) {
                    // Lỗi này không nên xảy ra nếu frontend gửi dữ liệu ID chính xác.
                    throw new ResourceNotFoundException(
                            "Không tìm thấy biến thể với ID: " + dto.getId() + " để cập nhật.");
                }
            } else {
                // Đây là một biến thể mới cần được tạo.
                variant = new ProductVariant();
            }

            // Cập nhật các thuộc tính của biến thể từ DTO.
            variant.setSku(dto.getSku());
            variant.setPrice(dto.getPrice());
            variant.setAttribute(dto.getAttribute());

            finalVariants.add(variant);
        }

        // Xóa các biến thể không còn tồn tại trong DTO list.
        product.getVariants().retainAll(finalVariants);

        // Thêm các biến thể mới (những biến thể chưa có trong collection ban đầu).
        for (ProductVariant finalVariant : finalVariants) {
            if (!product.getVariants().contains(finalVariant)) {
                product.addVariant(finalVariant);
            }
        }
    }

    private void deleteExistingImages(Product product, List<Long> imageIdsToDelete) {
        if (CollectionUtils.isEmpty(imageIdsToDelete)) {
            return;
        }

        List<ProductImage> imagesActuallyRemoved = new ArrayList<>();
        boolean mainImageWasDeleted = false;

        for (Long imageId : imageIdsToDelete) {
            Optional<ProductImage> imageOptional = product.getImages().stream()
                    .filter(img -> img.getId() != null && img.getId().equals(imageId))
                    .findFirst();

            if (imageOptional.isPresent()) {
                ProductImage imageToDelete = imageOptional.get();
                if (imageToDelete.isMain()) {
                    mainImageWasDeleted = true;
                }

                try {
                    String imageUrl = imageToDelete.getImageUrl();
                    if (imageUrl != null && imageUrl.startsWith("/storage/")) {
                        String relativePath = imageUrl.substring("/storage/".length());
                        fileStorageService.deleteFile(relativePath);
                    }
                } catch (Exception e) {

                }
                imagesActuallyRemoved.add(imageToDelete);
            }
        }

        if (!imagesActuallyRemoved.isEmpty()) {
            product.getImages().removeAll(imagesActuallyRemoved);
        }

        if (mainImageWasDeleted && !product.getImages().isEmpty()) {
            if (product.getImages().stream().noneMatch(ProductImage::isMain)) {
                product.getImages().get(0).setMain(true);
            }
        }
    }

    @Transactional
    public void deleteProduct(Long productId) throws Exception {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    return new ResourceNotFoundException("Không tìm thấy sản phẩm để xóa với ID: " + productId);
                });

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            for (ProductImage image : product.getImages()) {
                try {
                    String imageUrl = image.getImageUrl();
                    if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                        String relativePath = imageUrl.startsWith("/storage/")
                                ? imageUrl.substring("/storage/".length())
                                : imageUrl;
                        if (!relativePath.trim().isEmpty()) {
                            fileStorageService.deleteFile(relativePath);
                        }
                    }
                } catch (Exception e) {

                }
            }
        }

        productRepository.delete(product);
    }

    public Page<Product> searchProducts(String keyword, String categorySlug, String originSlug,
            Double minPrice, Double maxPrice,
            Boolean inStockOnly,
            String sortBy, String sortOrder, Pageable pageable) {

        Specification<Product> spec = Specification.where(ProductSpecification.isActive());

        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and(ProductSpecification.byKeyword(keyword));
        }
        if (categorySlug != null && !categorySlug.isEmpty()) {
            spec = spec.and(ProductSpecification.byCategorySlug(categorySlug));
        }
        if (originSlug != null && !originSlug.isEmpty()) {
            spec = spec.and(ProductSpecification.byOriginSlug(originSlug));
        }
        if (minPrice != null || maxPrice != null) {
            spec = spec.and(ProductSpecification.byPriceRange(minPrice, maxPrice));
        }

        if (inStockOnly != null) {
            spec = spec.and(ProductSpecification.byInStock(inStockOnly));
        }

        Sort sort = Sort.unsorted();
        if (sortBy != null && !sortBy.isEmpty()) {

            if ("desc".equalsIgnoreCase(sortOrder)) {
                sort = Sort.by(Sort.Direction.DESC, sortBy);
            } else {
                sort = Sort.by(Sort.Direction.ASC, sortBy);
            }
        }

        Pageable pageableWithSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        return productRepository.findAll(spec, pageableWithSort);
    }

    public Page<Product> findPaginatedByCategoryId(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable);
    }

    public Page<Product> findPaginatedByOriginId(Long originId, Pageable pageable) {
        return productRepository.findByOriginId(originId, pageable);
    }

    public Page<Product> findProductsByCriteria(ProductSearchCriteriaReq criteria, Pageable pageable) {
        Specification<Product> spec = ProductSpecification.searchProducts(criteria);

        return productRepository.findAll(spec, pageable);
    }

}
