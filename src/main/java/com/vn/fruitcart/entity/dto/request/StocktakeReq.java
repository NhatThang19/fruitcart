package com.vn.fruitcart.entity.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StocktakeReq {
    @NotNull(message = "Vui lòng chọn biến thể sản phẩm để kiểm kê.")
    private Long productVariantId;

    @NotNull(message = "Số lượng thực tế đếm được không được để trống.")
    @Min(value = 0, message = "Số lượng thực tế đếm được không được âm.")
    private Integer actualCountedQuantity;

    private String stocktakeNote;
}
