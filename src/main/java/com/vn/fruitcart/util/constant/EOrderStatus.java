package com.vn.fruitcart.util.constant;

import lombok.Getter;

@Getter
public enum EOrderStatus {
    PENDING("Chờ xác nhận", "bg-secondary"),
    PROCESSING("Đang xử lý", "bg-info text-dark"),
    SHIPPING("Đang giao hàng", "bg-primary"),
    COMPLETED("Hoàn thành", "bg-success"),
    CANCELLED("Đã hủy", "bg-danger");

    private final String displayName;
    private final String badgeClass;

    EOrderStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }
}
