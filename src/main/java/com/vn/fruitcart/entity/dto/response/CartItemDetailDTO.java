package com.vn.fruitcart.entity.dto.response;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CartItemDetailDTO {
    private Long id;
    private Integer quantity;
    private BigDecimal priceAtAddition;
    private BigDecimal subtotal;
    private ProductVariantDTO productVariant;
    private ProductDTO product;
    private String productImage;
}
