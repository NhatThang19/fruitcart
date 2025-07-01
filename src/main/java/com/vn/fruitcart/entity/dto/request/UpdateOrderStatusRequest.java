package com.vn.fruitcart.entity.dto.request;

import com.vn.fruitcart.util.constant.EOrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    @NotNull(message = "Trạng thái mới không được để trống")
    private EOrderStatus newStatus;
}