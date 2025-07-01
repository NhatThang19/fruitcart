package com.vn.fruitcart.entity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderCheckoutRequest {

    @NotBlank(message = "Tên người nhận không được để trống")
    private String customerName;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phoneNumber;

    // Trường này sẽ nhận địa chỉ chi tiết (số nhà, tên đường)
    @NotBlank(message = "Địa chỉ chi tiết không được để trống")
    private String streetAddress;

    // Các trường ẩn để lưu tên tỉnh/huyện/xã
    @NotBlank(message = "Vui lòng chọn Tỉnh/Thành phố")
    private String provinceName;

    @NotBlank(message = "Vui lòng chọn Quận/Huyện")
    private String districtName;

    @NotBlank(message = "Vui lòng chọn Phường/Xã")
    private String wardName;

    private String note;
}