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

    public PageMetadata buildAdminUserDetailPageMetadata() {
        List<PageMetadata.BreadcrumbSegment> breadcrumbList = new ArrayList<>();
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Quản lý người dùng", "/admin/users"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Chi tiết người dùng", null));
        return new PageMetadata("Chi tiết người dùng", breadcrumbList);
    }

    public PageMetadata buildAdminUserUpdatePageMetadata() {
        List<PageMetadata.BreadcrumbSegment> breadcrumbList = new ArrayList<>();
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Quản lý người dùng", "/admin/users"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Cập nhật người dùng", null));
        return new PageMetadata("Cập nhật người dùng", breadcrumbList);
    }

    public PageMetadata buildAdminCategoryPageMetadata() {
        List<PageMetadata.BreadcrumbSegment> breadcrumbList = new ArrayList<>();
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Quản lý danh mục", null));
        return new PageMetadata("Quản lý danh mục", breadcrumbList);
    }

    public PageMetadata buildAdminCategoryCreatePageMetadata() {
        List<PageMetadata.BreadcrumbSegment> breadcrumbList = new ArrayList<>();
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Quản lý danh mục", "/admin/categories"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Thêm danh mục", null));
        return new PageMetadata("Thêm danh mục", breadcrumbList);
    }

    public PageMetadata buildAdminCategoryUpdatePageMetadata() {
        List<PageMetadata.BreadcrumbSegment> breadcrumbList = new ArrayList<>();
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Quản lý danh mục", "/admin/categories"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Sửa danh mục", null));
        return new PageMetadata("Sửa danh mục", breadcrumbList);
    }

    public PageMetadata buildAdminCategoryDetailPageMetadata() {
        List<PageMetadata.BreadcrumbSegment> breadcrumbList = new ArrayList<>();
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Quản lý danh mục", "/admin/categories"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Chi tiết danh mục", null));
        return new PageMetadata("Chi tiết danh mục", breadcrumbList);
    }

    public PageMetadata buildAdminOriginPageMetadata() {
        List<PageMetadata.BreadcrumbSegment> breadcrumbList = new ArrayList<>();
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Quản lý nguồn gốc", null));
        return new PageMetadata("Quản lý nguồn gốc", breadcrumbList);
    }

    public PageMetadata buildAdminOriginCreatePageMetadata() {
        List<PageMetadata.BreadcrumbSegment> breadcrumbList = new ArrayList<>();
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Quản lý nguồn gốc", "/admin/origins"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Thêm nguồn gốc", null));
        return new PageMetadata("Thêm nguồn gốc", breadcrumbList);
    }

    public PageMetadata buildAdminOriginUpdatePageMetadata() {
        List<PageMetadata.BreadcrumbSegment> breadcrumbList = new ArrayList<>();
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Quản lý nguồn gốc", "/admin/origins"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Sửa nguồn gốc", null));
        return new PageMetadata("Sửa nguồn gốc", breadcrumbList);
    }

    public Object buildAdminOriginDetailPageMetadata() {
        List<PageMetadata.BreadcrumbSegment> breadcrumbList = new ArrayList<>();
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Quản lý nguồn gốc", "/admin/origins"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Chi tiết nguồn gốc", null));
        return new PageMetadata("Chi tiết nguồn gốc", breadcrumbList);
    }

    public Object demo() {
        List<PageMetadata.BreadcrumbSegment> breadcrumbList = new ArrayList<>();
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Home", "/admin"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Sản phẩm", null));
        return new PageMetadata("Sản phẩm", breadcrumbList);
    }

    public Object buildAdminProductCreate() {
        List<PageMetadata.BreadcrumbSegment> breadcrumbList = new ArrayList<>();
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Sản phẩm", "/admin/products"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Tạo sản phẩm", null));
        return new PageMetadata("Tạo sản phẩm", breadcrumbList);
    }

    public Object buildAdminProduct() {
        List<PageMetadata.BreadcrumbSegment> breadcrumbList = new ArrayList<>();
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Sản phẩm", null));
        return new PageMetadata("Sản phẩm", breadcrumbList);
    }

    public Object buildAdminDetailProduct() {
        List<PageMetadata.BreadcrumbSegment> breadcrumbList = new ArrayList<>();
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Sản phẩm", "/admin/products"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Chi tiết sản phẩm", null));
        return new PageMetadata("Chi tiết sản phẩm", breadcrumbList);
    }

    public Object buildAdminPurchaseOrder() {
        List<PageMetadata.BreadcrumbSegment> breadcrumbList = new ArrayList<>();
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Nhập hàng", "/admin/purchase-orders"));
        return new PageMetadata("Nhập hàng", breadcrumbList);
    }

    public Object buildAdminPurchaseOrderCreate() {
        List<PageMetadata.BreadcrumbSegment> breadcrumbList = new ArrayList<>();
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Nhập hàng", "/admin/purchase-orders"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Tạo đơn nhập hàng", null));
        return new PageMetadata("Tạo đơn nhập hàng", breadcrumbList);
    }

    public Object buildAdminPurchaseOrderDetail() {
        List<PageMetadata.BreadcrumbSegment> breadcrumbList = new ArrayList<>();
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Nhập hàng", "/admin/purchase-orders"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Chi tiết đơn nhập hàng", null));
        return new PageMetadata("Chi tiết đơn nhập hàng", breadcrumbList);
    }

    public Object buildAdminInventory() {
        List<PageMetadata.BreadcrumbSegment> breadcrumbList = new ArrayList<>();
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Kiểm kho", null));
        return new PageMetadata("Kiểm kho", breadcrumbList);
    }

    public Object buildAdminOrder() {
        List<PageMetadata.BreadcrumbSegment> breadcrumbList = new ArrayList<>();
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Đơn hàng", null));
        return new PageMetadata("Đơn hàng", breadcrumbList);
    }

    public Object buildAdminML() {
        List<PageMetadata.BreadcrumbSegment> breadcrumbList = new ArrayList<>();
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
        breadcrumbList.add(new PageMetadata.BreadcrumbSegment("Phân cụm", null));
        return new PageMetadata("Phân cụm", breadcrumbList);
    }

}
