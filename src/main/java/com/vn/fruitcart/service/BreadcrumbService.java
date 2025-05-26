package com.vn.fruitcart.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.vn.fruitcart.entity.dto.response.PageMetadata;

@Service
public class BreadcrumbService {
    public PageMetadata buildLoginPageMetadata() {
        List<PageMetadata.BreadcrumbSegment> breadcrumbList = new ArrayList<>();
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Trang chủ", "/"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Đăng nhập", null));
        return new PageMetadata("Đăng nhập", breadcrumbList);
    }

    public PageMetadata buildRegisterPageMetadata() {
        List<PageMetadata.BreadcrumbSegment> breadcrumbList = new ArrayList<>();
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Trang chủ", "/"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Đăng ký", null));
        return new PageMetadata("Đăng ký", breadcrumbList);
    }

    public PageMetadata buildUserProfilePageMetadata() {
        List<PageMetadata.BreadcrumbSegment> breadcrumbList = new ArrayList<>();
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Trang chủ", "/"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Hồ sơ cá nhân", null));
        return new PageMetadata("Hồ sơ cá nhân", breadcrumbList);
    }

}
