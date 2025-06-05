package com.vn.fruitcart.entity.dto.response;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProductVariantDTO {
    private Long id;
    private String attribute; 
    private BigDecimal price;
}
