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

    public PageMetadata buildUpdateUserProfilePageMetadata() {
        List<PageMetadata.BreadcrumbSegment> breadcrumbList = new ArrayList<>();
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Trang chủ", "/"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Hồ sơ cá nhân", "/profile"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Cập nhật", null));
        return new PageMetadata("Cập nhật hồ sơ cá nhân", breadcrumbList);
    }

    public PageMetadata buildChangeUserPassPageMetadata() {
        List<PageMetadata.BreadcrumbSegment> breadcrumbList = new ArrayList<>();
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Trang chủ", "/"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Hồ sơ cá nhân", "/profile"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Đổi mật khẩu", null));
        return new PageMetadata("Đổi mật khẩu", breadcrumbList);
    }

    public PageMetadata buildAdminUserLisPageMetadata() {
        List<PageMetadata.BreadcrumbSegment> breadcrumbList = new ArrayList<>();
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Quản lý người dùng", null));
        return new PageMetadata("Quản lý người dùng", breadcrumbList);
    }

}
