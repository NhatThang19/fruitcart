package com.vn.fruitcart.entity.dto.request.product;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductSearchCriteria {
    private String keyword;
    private String categorySlug;
    private String originSlug;
    private Double minPrice;
    private Double maxPrice;
    private Boolean inStockOnly;

    private String sortBy = "createdDate";
    private String sortOrder = "desc";
}
