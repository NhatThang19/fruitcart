package com.vn.fruitcart.entity.dto.request.product;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductSearchCriteriaReq {
    private String keyword;
    private Long categoryId;
    private Long originId;
    private Boolean status;
}
