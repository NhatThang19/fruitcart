package com.vn.fruitcart.entity.dto.request.product;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductVariantReq {
    @NotNull(message = "Giá biến thể không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá biến thể phải lớn hơn 0")
    private BigDecimal price;

    @NotBlank(message = "Thuộc tính biến thể không được để trống")
    @Size(max = 255, message = "Thuộc tính biến thể không được vượt quá 255 ký tự")
    private String attribute;
}
