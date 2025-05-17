package com.vn.fruitcart.entity.dto.response.product;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductViewResDTO {
    private Long id;
    private String productImgURL;
    private String name;
    private BigDecimal basePrice;
    private int productVariantCount;
    private String category;
    private boolean active;
    private Instant lastModifiedDate;
}
