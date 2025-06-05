package com.vn.fruitcart.entity.dto.request;

import lombok.Data;

@Data
public class AddToCartRequest {
    private Long productVariantId;
    private Integer quantity;
}