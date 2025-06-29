package com.vn.fruitcart.entity.dto.response.cart;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class CartDTO {
    private List<CartItemDTO> items;
    private BigDecimal totalPrice;
    private int itemCount;
}

