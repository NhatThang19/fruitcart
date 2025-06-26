package com.vn.fruitcart.entity.dto.request.product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
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
public class ProductUpdateReq {
    @NotNull(message = "ID sản phẩm không được để trống khi cập nhật")
    private Long id;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 255, message = "Tên sản phẩm không được vượt quá 255 ký tự")
    private String name;

    private String shortDescription;

    @Size(max = 10000, message = "Mô tả không được vượt quá 10000 ký tự")
    private String description;

    @NotNull(message = "Giá cơ bản không được để trống")
    @DecimalMin(value = "0.01", message = "Giá cơ bản phải lớn hơn 0")
    private BigDecimal basePrice;

    @NotNull(message = "ID Danh mục không được để trống")
    private Long categoryId;

    @NotNull(message = "ID Nguồn gốc không được để trống")
    private Long originId;

    private boolean isNew = true;

    private boolean status = true;

    @Valid
    private List<ProductVariantUpdateReq> variants = new ArrayList<>();

    private List<Long> imageIdsToDelete = new ArrayList<>();

    private Long mainImageId;

    private Integer newMainImageIndex;
}
