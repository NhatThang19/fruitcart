package com.vn.fruitcart.entity.dto.request.product;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductCreateReq {
  @NotBlank(message = "Tên sản phẩm không được để trống")
  @Size(max = 255, message = "Tên sản phẩm không được vượt quá 255 ký tự")
  private String name;

  private String shortDescription;

  private String description;

  @NotNull(message = "Giá cơ bản không được để trống")
  @DecimalMin(value = "0.0", inclusive = false, message = "Giá cơ bản phải lớn hơn 0")
  private BigDecimal basePrice;

  @NotNull(message = "Danh mục không được để trống")
  private Long categoryId;

  @NotNull(message = "Nguồn gốc không được để trống")
  private Long originId;

  private boolean isNew = true;

  private boolean status = true;

  @Valid
  private List<ProductVariantReq> variants;
}
