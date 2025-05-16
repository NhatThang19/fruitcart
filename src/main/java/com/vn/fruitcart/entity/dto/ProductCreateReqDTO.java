package com.vn.fruitcart.entity.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateReqDTO {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 255, message = "Tên sản phẩm không được vượt quá 255 ký tự")
    private String name;

    private String description;

    @NotNull(message = "Giá sản phẩm không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá sản phẩm phải lớn hơn 0")
    @Digits(integer = 10, fraction = 2, message = "Giá sản phẩm phải có tối đa 10 chữ số phần nguyên và 2 chữ số phần thập phân")
    private BigDecimal basePrice;

    private String slug;

    private boolean active = true;
    private boolean bulk = false;

    private String subcategory;

    @NotNull(message = "Danh mục phụ không được để trống")
    private Long subcategoryId;

    @Valid
    private List<ProductVariantDTO> variants = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductVariantDTO {
        @NotNull(message = "Giá sản phẩm không được để trống")
        @DecimalMin(value = "0.0", inclusive = false, message = "Giá sản phẩm phải lớn hơn 0")
        @Digits(integer = 10, fraction = 2, message = "Giá sản phẩm phải có tối đa 10 chữ số phần nguyên và 2 chữ số phần thập phân")
        private BigDecimal price;

        @NotBlank(message = "Thuộc tính không được để trống")
        private String attribute;
    }
}
