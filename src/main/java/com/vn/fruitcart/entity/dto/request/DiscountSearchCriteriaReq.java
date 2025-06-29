package com.vn.fruitcart.entity.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class DiscountSearchCriteriaReq {

    private String name;
    private Boolean active;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDateAfter;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDateBefore;
}
