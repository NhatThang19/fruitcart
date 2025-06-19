package com.vn.fruitcart.util.constant;

import lombok.Getter;

@Getter
public enum CustomerClusterEnum {
    NEW_OR_THRIFTY(0, "Khách Hàng Mới", "Nhóm khách hàng mới hoặc không hoạt động trong thời gian dài."),
    GOLD_CORE(1, "Khách Hàng Vàng", "Nhóm khách hàng giá trị và trung thành nhất."),
    DEVELOPING(2, "Khách Hàng Phát Triển", "Nhóm có tiềm năng lớn để trở thành khách hàng Vàng."),
    INFREQUENT_VISITOR(3, "Khách Hàng Ít Giao Dịch", "Nhóm khách hàng ít tương tác, mua với giá trị thấp."),
    UNKNOWN(-1, "Chưa phân loại", "Khách hàng chưa có đủ dữ liệu để phân loại.");

    private final int clusterNumber;
    private final String clusterName;
    private final String description;

    CustomerClusterEnum(int clusterNumber, String clusterName, String description) {
        this.clusterNumber = clusterNumber;
        this.clusterName = clusterName;
        this.description = description;
    }

    public static CustomerClusterEnum fromClusterNumber(int number) {
        for (CustomerClusterEnum value : values()) {
            if (value.getClusterNumber() == number) {
                return value;
            }
        }
        return UNKNOWN;
    }
}
