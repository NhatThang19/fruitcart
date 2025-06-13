package com.vn.fruitcart.entity.dto.response.user;

import java.math.BigDecimal;
import java.util.List;

import com.vn.fruitcart.entity.Order;
import com.vn.fruitcart.entity.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDetailRes {
    private User user;

    private List<Order> orderHistory;

    private long totalOrders;
    private BigDecimal totalSpending;
    private BigDecimal averageOrderValue;
    private long purchaseFrequencyLast90Days;
    private Long daysSinceLastPurchase;
}
