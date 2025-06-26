package com.vn.fruitcart.util.mapper;

import com.vn.fruitcart.entity.Product;
import com.vn.fruitcart.entity.ProductImage;
import com.vn.fruitcart.entity.ProductVariant;
import com.vn.fruitcart.entity.dto.request.product.ProductUpdateReq;
import com.vn.fruitcart.entity.dto.request.product.ProductVariantUpdateReq;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;


@Component
public class ProductMapper {


    public ProductUpdateReq toProductUpdateReq(Product product) {
        if (product == null) {
            return null;
        }

        ProductUpdateReq dto = new ProductUpdateReq();

        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setShortDescription(product.getShortDescription());
        dto.setDescription(product.getDescription());
        dto.setBasePrice(product.getBasePrice());
        dto.setNew(product.isNew());
        dto.setStatus(product.isStatus());

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
        }
        if (product.getOrigin() != null) {
            dto.setOriginId(product.getOrigin().getId());
        }

        if (product.getVariants() != null) {
            dto.setVariants(product.getVariants().stream()
                    .map(this::toProductVariantUpdateReq)
                    .collect(Collectors.toList()));
        }

        product.getImages().stream()
                .filter(ProductImage::isMain)
                .findFirst()
                .ifPresent(mainImage -> dto.setMainImageId(mainImage.getId()));

        return dto;
    }

    private ProductVariantUpdateReq toProductVariantUpdateReq(ProductVariant variant) {
        if (variant == null) {
            return null;
        }
        ProductVariantUpdateReq dto = new ProductVariantUpdateReq();
        dto.setId(variant.getId());
        dto.setAttribute(variant.getAttribute());
        dto.setPrice(variant.getPrice());

        return dto;
    }
}