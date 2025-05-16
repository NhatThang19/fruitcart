package com.vn.fruitcart.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import com.vn.fruitcart.entity.Category;
import com.vn.fruitcart.entity.Product;
import com.vn.fruitcart.entity.dto.response.PageMetadata;
import com.vn.fruitcart.service.CategoryService;
import com.vn.fruitcart.service.ProductImageService;
import com.vn.fruitcart.service.ProductService;
import com.vn.fruitcart.service.SubCategoryService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ProductController {

    private final ProductService productService;
    private final SubCategoryService subcategoryService;
    private final CategoryService categoryService;
    private final ProductImageService productImageService;

    public ProductController(ProductService productService, SubCategoryService subcategoryService,
            CategoryService categoryService, ProductImageService productImageService) {
        this.productService = productService;
        this.subcategoryService = subcategoryService;
        this.categoryService = categoryService;
        this.productImageService = productImageService;
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

        model.addAttribute("product", new Product());

        return "admin/pages/products/create";
    }

    @PostMapping("/admin/products/create")
    public String saveProduct(
            @Valid @ModelAttribute("product") Product product,
            BindingResult result,
            @RequestParam("mainImage") MultipartFile mainImage,
            @RequestParam(value = "thumbnailFiles", required = false) MultipartFile[] thumbnailFiles,
            Model model) {

        if (mainImage == null || mainImage.isEmpty()) {
            result.rejectValue("mainImage", "", "Ảnh chính không được để trống.");
        }
        
        if (result.hasErrors()) {

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
        try {
            product.setSlug(categoryService.generateUniqueSlug(product.getSlug()));

            Product savedProduct = productService.save(product);

            if (mainImage != null && !mainImage.isEmpty()) {
                productImageService.saveProductImages(savedProduct, mainImage, thumbnailFiles);
            }

            return "redirect:/admin/products?success";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi lưu sản phẩm: " + e.getMessage());
            model.addAttribute("categories", categoryService.findAllActiveCategories());
            return "admin/pages/products/create";
        }
    }

}
