package com.vn.fruitcart.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class SalesDataDto {
    private String month;
    private BigDecimal revenue;
}