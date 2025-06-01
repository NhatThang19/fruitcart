package com.vn.fruitcart.entity.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderCreateReq {
    private String note;

    @Valid
    @NotEmpty(message = "Đơn nhập hàng phải có ít nhất một sản phẩm")
    @Size(min = 1, message = "Đơn nhập hàng phải có ít nhất một sản phẩm")
    private List<PurchaseOrderItemReq> items;
}
