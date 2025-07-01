package com.vn.fruitcart.util.constant;

import lombok.Getter;

@Getter
public enum OrderStatusEnum {
    PENDING("Đang chờ xử lý"),
    CONFIRMED("Đã xác nhận"),
    DELIVERING("Đang giao hàng"),
    DELIVERED("Đã giao thành công"),
    CANCELLED("Đã hủy");

    private final String displayName;

    OrderStatusEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
