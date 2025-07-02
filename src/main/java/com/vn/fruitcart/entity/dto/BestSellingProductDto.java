package com.vn.fruitcart.entity.dto;

import com.vn.fruitcart.entity.Product;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BestSellingProductDto {
    private Product product;
    private Long totalSold;

    public BestSellingProductDto(Product product, Long totalSold) {
        this.product = product;
        this.totalSold = (totalSold != null) ? totalSold : 0L;
    }
}
