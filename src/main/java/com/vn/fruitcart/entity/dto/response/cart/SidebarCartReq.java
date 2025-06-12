package com.vn.fruitcart.entity.dto.response.cart;

import java.math.BigDecimal;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SidebarCartReq {
    private List<SidebarCartItemReq> items;
    private int totalItems;
    private BigDecimal totalPrice;
}
