package com.vn.fruitcart.entity.dto.response.cart;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CartItemDTO {
    private Long cartItemId;
    private String productName;
    private String productSlug;
    private String variantAttribute;
    private BigDecimal price;
    private int quantity;
    private String imageUrl;
}
