package com.vn.fruitcart.entity.dto.response;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CartItemDetailDTO {
    private Long cartItemId;
    private Long productId;
    private String productName;
    private Long variantId;
    private String variantAttribute;
    private BigDecimal price;
    private int quantity;
    private int stock;
    private BigDecimal totalPrice;
    private String productImageUrl;
}
