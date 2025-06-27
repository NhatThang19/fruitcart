package com.vn.fruitcart.entity.dto.request.inventory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StocktakeReq {
    @Valid
    @NotEmpty(message = "Phiếu kiểm kê phải có ít nhất một sản phẩm.")
    private List<StocktakeItemReq> items = new ArrayList<>();

    private String stocktakeNote;
}
