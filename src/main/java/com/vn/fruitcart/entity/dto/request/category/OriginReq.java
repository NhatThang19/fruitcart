package com.vn.fruitcart.entity.dto.request.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OriginReq {
    @NotBlank(message = "Tên nguồn gốc không được để trống")
    @Size(min = 2, max = 255, message = "Tên nguồn gốc phải từ 2 đến 255 ký tự")
    private String name;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;

    private Boolean status = true;
}
