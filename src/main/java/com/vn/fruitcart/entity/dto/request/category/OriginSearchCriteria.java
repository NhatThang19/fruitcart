package com.vn.fruitcart.entity.dto.request.category;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OriginSearchCriteria {
    private String keyword;
    private Boolean status;
}
