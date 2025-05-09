package com.vn.fruitcart.entity.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PageMetadata {
    private String title;
    private List<BreadcrumbSegment> breadcrumbs;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BreadcrumbSegment {
        private String name;
        private String url;
    }
}
