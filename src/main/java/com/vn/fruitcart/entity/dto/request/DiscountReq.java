package com.vn.fruitcart.entity.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class DiscountReq {

    private Long id; // Sẽ là null khi tạo mới, có giá trị khi cập nhật

    @NotBlank(message = "Tên chương trình không được để trống")
    @Size(max = 100, message = "Tên chương trình không được vượt quá 100 ký tự")
    private String name;

    @NotNull(message = "Tỷ lệ giảm giá không được để trống")
    @DecimalMin(value = "0.1", message = "Tỷ lệ giảm giá phải lớn hơn 0")
    private BigDecimal discountPercentage;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    private boolean active = true;

    private List<Long> variantIds;

    @AssertTrue(message = "Ngày kết thúc phải sau ngày bắt đầu")
    public boolean isEndDateAfterStartDate() {
        return endDate == null || startDate == null || endDate.isAfter(startDate);
    }
}