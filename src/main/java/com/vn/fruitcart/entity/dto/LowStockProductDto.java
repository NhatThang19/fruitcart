package com.vn.fruitcart.entity.dto;

import com.vn.fruitcart.entity.Product;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LowStockProductDto {
    private Product product;
    private Long totalStock;

    public LowStockProductDto(Product product, Long totalStock) {
        this.product = product;
        this.totalStock = (totalStock != null) ? totalStock : 0L;
    }
}
