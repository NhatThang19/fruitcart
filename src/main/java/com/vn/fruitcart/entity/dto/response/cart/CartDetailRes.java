package com.vn.fruitcart.entity.dto.response.cart;

import com.vn.fruitcart.entity.dto.response.CartItemDetailDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class CartDetailRes {
    private List<CartItemDetailDTO> items;
    private BigDecimal totalPrice;
    private int totalItems;
}
