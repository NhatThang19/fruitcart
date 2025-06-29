package com.vn.fruitcart.entity.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class QuickViewProductDTO {
    private String name;
    private String shortDescription;
    private String mainImage;
    private List<VariantInfo> variants;

    @Getter
    @Setter
    public static class VariantInfo {
        private Long id;
        private String attribute;
        private BigDecimal price;
        private BigDecimal salePrice;
        private boolean onSale;
        private int quantity;
    }
}