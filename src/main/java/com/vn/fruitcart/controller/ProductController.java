package com.vn.fruitcart.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import com.vn.fruitcart.entity.Category;
import com.vn.fruitcart.entity.dto.response.PageMetadata;
import com.vn.fruitcart.service.CategoryService;
import com.vn.fruitcart.service.ProductService;
import com.vn.fruitcart.service.SubCategoryService;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProductController {

    private final ProductService productService;
    private final SubCategoryService subcategoryService;
    private final CategoryService categoryService;

    public ProductController(ProductService productService, SubCategoryService subcategoryService,
            CategoryService categoryService) {
        this.productService = productService;
        this.subcategoryService = subcategoryService;
        this.categoryService = categoryService;
    }

    @GetMapping("/admin/products/create")
    public String getCreateProductPage(Model model) {
        List<PageMetadata.BreadcrumbSegment> segments = new ArrayList<>();
        segments.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
        segments.add(new PageMetadata.BreadcrumbSegment("Sản phẩm", "/admin/products"));
        segments.add(new PageMetadata.BreadcrumbSegment("Tạo sản phẩm", "/admin/products/create"));

        PageMetadata pageMetadata = new PageMetadata("Tạo sản phẩm", segments);
        model.addAttribute("pageMetadata", pageMetadata);

        List<Category> categories = categoryService.findAllActiveCategories();
        model.addAttribute("categories", categories);

        return "admin/pages/products/create";
    }

}
