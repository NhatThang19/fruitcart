package com.vn.fruitcart.entity.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OrderCheckoutRequest {

    @NotEmpty(message = "Vui lòng nhập họ tên người nhận.")
    @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự.")
    private String receiverName;

    @NotEmpty(message = "Vui lòng nhập địa chỉ giao hàng.")
    private String shippingAddress;

    @NotEmpty(message = "Vui lòng nhập số điện thoại.")
    @Size(min = 10, max = 15, message = "Số điện thoại không hợp lệ.")
    private String phoneNumber;

    private String notes; // Ghi chú thêm (không bắt buộc)
}
