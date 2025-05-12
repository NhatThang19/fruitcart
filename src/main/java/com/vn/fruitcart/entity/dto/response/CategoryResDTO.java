package com.vn.fruitcart.entity.dto.response;

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
public class CategoryResDTO {
    private Long id;
    private String name;
    private String slug;
    private boolean active;
    private int subCategoryCount;
    private String subCategoryNames;
    private Instant lastModifiedDate;
}