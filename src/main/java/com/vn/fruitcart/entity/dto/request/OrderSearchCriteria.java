package com.vn.fruitcart.entity.dto.request;

import com.vn.fruitcart.util.constant.EOrderStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class OrderSearchCriteria {
    private String keyword;

    // Lọc theo trạng thái
    private EOrderStatus status;

    // Lọc theo khoảng ngày đặt hàng
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;
}