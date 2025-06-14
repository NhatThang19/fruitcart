package com.vn.fruitcart.util.constant;

import jakarta.persistence.Transient;
import lombok.Getter;

@Getter
public enum CustomerClusterEnum {
    NEW_OR_THRIFTY(0, "Khách Hàng Mới/Tiết Kiệm", "Nhóm khách hàng mới hoặc không hoạt động trong thời gian dài."),
    GOLD_CORE(1, "Khách Hàng Vàng/Nòng Cốt", "Nhóm khách hàng giá trị và trung thành nhất."),
    DEVELOPING(2, "Khách Hàng Phát Triển", "Nhóm có tiềm năng lớn để trở thành khách hàng Vàng."),
    INFREQUENT_VISITOR(3, "Khách Hàng Ít Giao Dịch/Vãng Lai", "Nhóm khách hàng ít tương tác, mua với giá trị thấp."),
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

    /**
     * Lấy ra đối tượng Enum tương ứng với clusterNumber.
     * 
     * @Transient để JPA không map trường này vào DB.
     * @return CustomerClusterEnum
     */
    @Transient
    public CustomerClusterEnum getClusterEnum() {
        return CustomerClusterEnum.fromClusterNumber(this.clusterNumber);
    }

    /**
     * Lấy ra tên cụm để hiển thị trực quan.
     * 
     * @return String tên cụm
     */
    @Transient
    public String getClusterName() {
        return getClusterEnum().getClusterName();
    }

    /**
     * Lấy ra mô tả chi tiết của cụm.
     * 
     * @return String mô tả
     */
    @Transient
    public String getClusterDescription() {
        return getClusterEnum().getDescription();
    }
}
