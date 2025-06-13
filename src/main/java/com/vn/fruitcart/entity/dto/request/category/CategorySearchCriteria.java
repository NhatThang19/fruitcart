package com.vn.fruitcart.entity.dto.request.category;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategorySearchCriteria {
    private String keyword;
    private Boolean status;
}
