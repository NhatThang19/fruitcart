package com.vn.fruitcart.entity.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderItemReq {
    @NotNull(message = "ID biến thể sản phẩm không được để trống")
    private Long productVariantId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải ít nhất là 1")
    private Integer quantity;

    @NotNull(message = "Đơn giá không được để trống")
    @Min(value = 0, message = "Đơn giá không được âm")
    private BigDecimal unitPrice;

    private String note;
}
