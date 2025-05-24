package com.vn.fruitcart.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import com.vn.fruitcart.entity.Category;
import com.vn.fruitcart.entity.Product;
import com.vn.fruitcart.entity.ProductVariant;
import com.vn.fruitcart.entity.SubCategory;
import com.vn.fruitcart.entity.dto.ProductCreateReqDTO;
import com.vn.fruitcart.entity.dto.ProductCreateReqDTO.ProductVariantDTO;
import com.vn.fruitcart.entity.dto.response.PageMetadata;
import com.vn.fruitcart.service.CategoryService;
import com.vn.fruitcart.service.ProductImageService;
import com.vn.fruitcart.service.ProductService;
import com.vn.fruitcart.service.SubCategoryService;
import com.vn.fruitcart.util.StringUtil;
import com.vn.fruitcart.util.mapper.ProductMapper;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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

        model.addAttribute("pReqDTO", new ProductCreateReqDTO());

        return "admin/pages/products/create";
    }

    @PostMapping("/admin/products/create")
    public String saveProduct(
            @Valid @ModelAttribute("pReqDTO") ProductCreateReqDTO pReqDTO,
            BindingResult result,
            @RequestParam("mainImage") MultipartFile mainImage,
            @RequestParam(value = "thumbnailFiles", required = false) MultipartFile[] thumbnailFiles,
            Model model) {

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
            Product newProduct = ProductMapper.toProductEntity(pReqDTO);

            newProduct.setSlug(StringUtil.toSlug(pReqDTO.getName()));
            SubCategory subCategory = subcategoryService.getSubCategoryById(pReqDTO.getSubcategoryId());
            newProduct.setSubcategory(subCategory);

            Product savedProduct = productService.save(newProduct);

            if (mainImage != null && !mainImage.isEmpty()) {
                productImageService.saveProductImages(savedProduct, mainImage,
                        thumbnailFiles);
            }

            List<ProductVariant> variants = ProductMapper.toProductVariantEntities(pReqDTO.getVariants(), savedProduct);
            savedProduct.setVariants(variants);

            return "redirect:/admin/products?success";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi lưu sản phẩm: " + e.getMessage());
            model.addAttribute("categories", categoryService.findAllActiveCategories());
            return "admin/pages/products/create";
        }
    }

    @GetMapping("/admin/products")
    public String getAdminProductPage(Model model) {
        List<PageMetadata.BreadcrumbSegment> segments = new ArrayList<>();
        segments.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
        segments.add(new PageMetadata.BreadcrumbSegment("Sản phẩm", "/admin/products"));

        PageMetadata pageMetadata = new PageMetadata("Sản phẩm", segments);
        model.addAttribute("pageMetadata", pageMetadata);

        return "admin/pages/products/view";
    }

    @GetMapping("/admin/products/edit")
    public String showEditForm() {
        Product p = productService.findById(1L);
        String z = p.getName();      
        return "admin/product/edit";
    }

}
