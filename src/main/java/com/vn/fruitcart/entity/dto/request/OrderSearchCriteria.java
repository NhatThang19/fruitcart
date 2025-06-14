package com.vn.fruitcart.entity.dto.request;

import com.vn.fruitcart.util.constant.OrderStatusEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderSearchCriteria {
    private String keyword;
    private OrderStatusEnum status;
}
