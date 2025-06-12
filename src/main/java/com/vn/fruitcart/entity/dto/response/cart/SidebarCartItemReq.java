package com.vn.fruitcart.entity.dto.response.cart;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SidebarCartItemReq {
    private Long id;
    private Long itemId;
    private String name;
    private String attribute;
    private int quantity;
    private BigDecimal totalPrice;
    private String imgUrl;
}
