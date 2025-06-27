package com.vn.fruitcart.entity.dto.request.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StocktakeItemReq {

    @NotNull(message = "Vui lòng chọn một sản phẩm.")
    private Long productVariantId;

    @NotNull(message = "Số lượng đếm được không được để trống.")
    @Min(value = 0, message = "Số lượng đếm được phải là một số không âm.")
    private Integer actualCountedQuantity;
}