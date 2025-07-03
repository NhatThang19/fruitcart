package com.vn.fruitcart.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.vn.fruitcart.entity.dto.BestSellingProductDto;
import com.vn.fruitcart.entity.dto.LowStockProductDto;
import com.vn.fruitcart.entity.dto.request.product.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import com.vn.fruitcart.exception.ResourceNotFoundException;
import com.vn.fruitcart.repository.CategoryRepository;
import com.vn.fruitcart.repository.OriginRepository;
import com.vn.fruitcart.repository.ProductRepository;
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

    private static final String DEFAULT_IMAGES_DIRECTORY = "/default/";

    @Transactional
    public Product createProduct(ProductCreateReq productDTO, List<MultipartFile> images, List<Boolean> isMainFlags) {

        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy danh mục với ID: " + productDTO.getCategoryId()));

        Origin origin = originRepository.findById(productDTO.getOriginId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy nguồn gốc với ID: " + productDTO.getOriginId()));

        Product product = new Product();
        product.setName(productDTO.getName());
        product.setSlug(FruitCartUtils.toSlug(productDTO.getName()));
        product.setShortDescription(productDTO.getShortDescription());

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

        if (!CollectionUtils.isEmpty(images)) {
            List<ProductImage> uploadedImages = new ArrayList<>();
            boolean mainImageWasSet = false;

            for (int i = 0; i < images.size(); i++) {
                MultipartFile imageFile = images.get(i);
                if (imageFile != null && !imageFile.isEmpty()) {
                    String imageUrl = fileStorageService.storeFile(imageFile, "product_images");

                    ProductImage productImage = new ProductImage();
                    productImage.setImageUrl(imageUrl);

                    if (!mainImageWasSet && isMainFlags != null && i < isMainFlags.size() && isMainFlags.get(i)) {
                        productImage.setMain(true);
                        mainImageWasSet = true;
                    } else {
                        productImage.setMain(false);
                    }

                    uploadedImages.add(productImage);
                }
            }

            if (!mainImageWasSet && !uploadedImages.isEmpty()) {
                uploadedImages.get(0).setMain(true);
            }

            uploadedImages.forEach(product::addImage);
        }

        if (product.getImages().isEmpty()) {
            ProductImage defaultImage = new ProductImage();
            defaultImage.setImageUrl("/storage/default/product.jpg");
            defaultImage.setMain(true);

            product.addImage(defaultImage);
        }

        return productRepository.save(product);
    }

    private void handleProductImagesUpload(Product product, List<MultipartFile> newImages,
                                           Long mainImageId, Integer newMainImageIndex) {

        List<MultipartFile> filesToUpload = newImages != null ?
                newImages.stream().filter(f -> f != null && !f.isEmpty()).toList() :
                new ArrayList<>();

        if (filesToUpload.isEmpty()) {
            if (newMainImageIndex == null && mainImageId != null) {
                setMainImageForExisting(product, mainImageId);
            }
            return;
        }

        List<ProductImage> newlyUploadedImages = new ArrayList<>();
        for (MultipartFile imageFile : filesToUpload) {
            String imageUrl = fileStorageService.storeFile(imageFile, "product_images");
            ProductImage productImage = new ProductImage();
            productImage.setImageUrl(imageUrl);
            product.addImage(productImage);
            newlyUploadedImages.add(productImage);
        }


        product.getImages().forEach(img -> img.setMain(false));

        if (newMainImageIndex != null && newMainImageIndex < newlyUploadedImages.size()) {
            newlyUploadedImages.get(newMainImageIndex).setMain(true);
        } else if (mainImageId != null) {
            setMainImageForExisting(product, mainImageId);
        }
    }

    private void setMainImageForExisting(Product product, Long mainImageIdToSet) {
        product.getImages().forEach(img -> img.setMain(false));

        product.getImages().stream()
                .filter(img -> img.getId() != null && img.getId().equals(mainImageIdToSet))
                .findFirst()
                .ifPresent(img -> img.setMain(true));

        boolean hasMainImage = product.getImages().stream().anyMatch(ProductImage::isMain);
        if (!hasMainImage && !product.getImages().isEmpty()) {
            product.getImages().get(0).setMain(true);
        }
    }

    public Product getProductById(Long id) throws ResourceNotFoundException {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));
    }

    @Transactional
    public Product updateProduct(Long productId, ProductUpdateReq productDTO,
                                 List<MultipartFile> newImages,
                                 List<Long> imageIdsToDelete) {

        if (!Objects.equals(productId, productDTO.getId())) {
            throw new IllegalArgumentException("ID sản phẩm không khớp để cập nhật.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + productId));

        updateBasicProductInfo(product, productDTO);

        updateProductAssociations(product, productDTO);

        updateProductVariants(product, productDTO.getVariants());
        deleteExistingImages(product, imageIdsToDelete);
        handleProductImagesUpload(product, newImages, productDTO.getMainImageId(), productDTO.getNewMainImageIndex());

        ensureMainImageExists(product);

        return productRepository.save(product);
    }

    private void updateBasicProductInfo(Product product, ProductUpdateReq dto) {
        product.setName(dto.getName());
        product.setSlug(FruitCartUtils.toSlug(dto.getName()));
        product.setDescription(dto.getDescription());
        product.setShortDescription(dto.getShortDescription());
        product.setBasePrice(dto.getBasePrice());
        product.setNew(dto.isNew());
        product.setStatus(dto.isStatus());
    }

    private void updateProductAssociations(Product product, ProductUpdateReq dto) {
        if (!Objects.equals(product.getCategory().getId(), dto.getCategoryId())) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + dto.getCategoryId()));
            product.setCategory(category);
        }

        if (!Objects.equals(product.getOrigin().getId(), dto.getOriginId())) {
            Origin origin = originRepository.findById(dto.getOriginId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nguồn gốc với ID: " + dto.getOriginId()));
            product.setOrigin(origin);
        }
    }

    private void ensureMainImageExists(Product product) {
        if (!product.getImages().isEmpty() && product.getImages().stream().noneMatch(ProductImage::isMain)) {
            product.getImages().get(0).setMain(true);
        }
    }

    @Transactional
    protected void updateProductVariants(Product product, List<ProductVariantUpdateReq> variantDTOs) {
        if (variantDTOs == null) {
            product.getVariants().clear();
            return;
        }

        Set<Long> dtoVariantIds = variantDTOs.stream()
                .map(ProductVariantUpdateReq::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        product.getVariants().removeIf(variant -> !dtoVariantIds.contains(variant.getId()));

        for (ProductVariantUpdateReq dto : variantDTOs) {
            if (dto.getId() != null && dto.getId() != 0) {
                product.getVariants().stream()
                        .filter(v -> v.getId().equals(dto.getId()))
                        .findFirst()
                        .ifPresent(variant -> {
                            variant.setAttribute(dto.getAttribute());
                            variant.setPrice(dto.getPrice());
                        });
            } else {
                ProductVariant newVariant = new ProductVariant();
                newVariant.setAttribute(dto.getAttribute());
                newVariant.setPrice(dto.getPrice());
                product.addVariant(newVariant);
            }
        }
        if (product.getVariants().isEmpty()) {
            ProductVariant defaultVariant = new ProductVariant();
            defaultVariant.setPrice(product.getBasePrice());
            defaultVariant.setAttribute("Mặc định");

            product.addVariant(defaultVariant);
        }
    }

    private void deleteExistingImages(Product product, List<Long> imageIdsToDelete) {
        if (CollectionUtils.isEmpty(imageIdsToDelete)) {
            return;
        }

        Map<Long, ProductImage> imageMap = product.getImages().stream()
                .filter(img -> img.getId() != null)
                .collect(Collectors.toMap(ProductImage::getId, img -> img));

        List<ProductImage> imagesToRemove = new ArrayList<>();

        for (Long imageId : imageIdsToDelete) {
            ProductImage imageToDelete = imageMap.get(imageId);
            if (imageToDelete != null) {
                String imageUrl = imageToDelete.getImageUrl();
                if (imageUrl != null && !imageUrl.contains(DEFAULT_IMAGES_DIRECTORY)) {
                    // Logic lấy relative path và xóa
                    String relativePath = imageUrl.startsWith("/storage/")
                            ? imageUrl.substring("/storage/".length())
                            : imageUrl;
                    if (!relativePath.trim().isEmpty()) {
                        fileStorageService.deleteFile(relativePath);
                    }
                }
                imagesToRemove.add(imageToDelete);
            }
        }

        if (!imagesToRemove.isEmpty()) {
            product.getImages().removeAll(imagesToRemove);
        }
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm để xóa với ID: " + productId));

        if (product.getImages() != null && !product.getImages().isEmpty()) {

            product.getImages().forEach(image -> {
                String imageUrl = image.getImageUrl();

                if (imageUrl == null || imageUrl.trim().isEmpty()) {
                    return;
                }

                String normalizedUrl = imageUrl.trim();
                if (!normalizedUrl.startsWith("/")) {
                    normalizedUrl = "/" + normalizedUrl;
                }

                if (normalizedUrl.contains(DEFAULT_IMAGES_DIRECTORY)) {
                    return;
                }

                try {
                    String relativePath = imageUrl.startsWith("/storage/")
                            ? imageUrl.substring("/storage/".length())
                            : imageUrl;

                    if (!relativePath.trim().isEmpty()) {
                        fileStorageService.deleteFile(relativePath);
                    }
                } catch (Exception ignored) {

                }
            });
        }
        productRepository.delete(product);
    }

    public Page<Product> searchProducts(ProductSearchCriteria criteria, Pageable pageable) {

        Specification<Product> spec = Specification.where(ProductSpecification.isActive());

        if (StringUtils.hasText(criteria.getKeyword())) {
            spec = spec.and(ProductSpecification.byKeyword(criteria.getKeyword()));
        }
        if (StringUtils.hasText(criteria.getCategorySlug())) {
            spec = spec.and(ProductSpecification.byCategorySlug(criteria.getCategorySlug()));
        }
        if (StringUtils.hasText(criteria.getOriginSlug())) {
            spec = spec.and(ProductSpecification.byOriginSlug(criteria.getOriginSlug()));
        }
        if (criteria.getMinPrice() != null || criteria.getMaxPrice() != null) {
            spec = spec.and(ProductSpecification.byPriceRange(criteria.getMinPrice(), criteria.getMaxPrice()));
        }

        if (criteria.getInStockOnly() != null && criteria.getInStockOnly()) {
            spec = spec.and(ProductSpecification.byInStock(true));
        }
        if (criteria.getIsNew() != null && criteria.getIsNew()) {
            spec = spec.and(ProductSpecification.byIsNew(true));
        }
        if (criteria.getOnSale() != null && criteria.getOnSale()) {
            spec = spec.and(ProductSpecification.byOnSale(true));
        }

        return productRepository.findAll(spec, pageable);
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

    public Product getProductBySlug(String slug) {
        return productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với slug: " + slug));
    }

    public List<BestSellingProductDto> findTopBestSellingProducts(int limit) {
        Pageable topProducts = PageRequest.of(0, limit);
        return productRepository.findBestSellingProducts(topProducts).getContent();
    }

    public List<LowStockProductDto> findLowStockProducts(int stockThreshold) {
        return productRepository.findLowStockProducts(stockThreshold);
    }

    public List<Product> findOnSaleProducts() {
        return productRepository.findOnSaleProducts(LocalDateTime.now());
    }

}
