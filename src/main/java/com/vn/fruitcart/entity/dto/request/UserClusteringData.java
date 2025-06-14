package com.vn.fruitcart.entity.dto.request;

import java.math.BigDecimal;

import com.vn.fruitcart.entity.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserClusteringData {
    private User user;
    private BigDecimal totalSpending;
    private long totalOrders;
    private BigDecimal averageOrderValue;
    private long purchaseFrequencyLast90Days;
    private Long daysSinceLastPurchase;
    private int assignedCluster;
}
