package com.vn.fruitcart.util.mapper;

import java.util.ArrayList;
import java.util.List;

import com.vn.fruitcart.entity.Product;
import com.vn.fruitcart.entity.ProductVariant;
import com.vn.fruitcart.entity.dto.ProductCreateReqDTO;
import com.vn.fruitcart.util.StringUtil;

public class ProductMapper {
    public static Product toProductEntity(ProductCreateReqDTO dto) {
        if (dto == null)
            return null;

        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setBasePrice(dto.getBasePrice());
        product.setActive(dto.isActive());
        product.setBulk(dto.isBulk());

        return product;
    }

    /**
     * Chuyển list ProductVariantDTO thành list ProductVariant entity,
     * đồng thời gán quan hệ với Product.
     */
    public static List<ProductVariant> toProductVariantEntities(
            List<ProductCreateReqDTO.ProductVariantDTO> variantDTOs, Product product) {

        if (variantDTOs == null || variantDTOs.isEmpty())
            return new ArrayList<>();

        List<ProductVariant> variants = new ArrayList<>();
        Long productId = product.getId();

        for (int i = 0; i < variantDTOs.size(); i++) {
            ProductCreateReqDTO.ProductVariantDTO dto = variantDTOs.get(i);

            ProductVariant variant = new ProductVariant();
            variant.setAttribute(dto.getAttribute());
            variant.setPrice(dto.getPrice());
            variant.setProduct(product);

            String normalizedName = StringUtil.toSlug(product.getName());
            String variantCode = String.format("%02d", i + 1);

            String generatedSku = productId + "-" + normalizedName + "-" + variantCode;
            variant.setSku(generatedSku);

            variants.add(variant);
        }

        return variants;
    }
}
