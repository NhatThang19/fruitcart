package com.vn.fruitcart.controller.client;

import java.util.Collections;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.vn.fruitcart.entity.Product;
import com.vn.fruitcart.service.BreadcrumbService;
import com.vn.fruitcart.service.CategoryService;
import com.vn.fruitcart.service.OriginService;
import com.vn.fruitcart.service.ProductService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final OriginService originService;
    private final BreadcrumbService breadcrumbService;

    @GetMapping("/")
    public String getHomePage(Model model) {
        boolean hideBreadcrumb = true;
        model.addAttribute("hideBreadcrumb", hideBreadcrumb);
        return "client/pages/index";
    }

    @GetMapping("/products")
    public String index(
            Model model,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "category", required = false) String categorySlug,
            @RequestParam(name = "origin", required = false) String originSlug,
            @RequestParam(name = "minPrice", required = false) Double minPrice,
            @RequestParam(name = "maxPrice", required = false) Double maxPrice,
            @RequestParam(name = "inStock", required = false) Boolean inStockOnly,
            @RequestParam(name = "sortBy", defaultValue = "createdDate") String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = "desc") String sortOrder,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "12") int pageSize) {

        Pageable pageable = PageRequest.of(page - 1, pageSize);

        Page<Product> productPage = productService.searchProducts(
                keyword, categorySlug, originSlug, minPrice, maxPrice,
                inStockOnly, sortBy, sortOrder, pageable);

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", productPage.getNumber() + 1);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());

        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", categorySlug);
        model.addAttribute("selectedOrigin", originSlug);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("inStockOnly", inStockOnly);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortOrder", sortOrder);
        model.addAttribute("pageSize", pageSize);

        try {
            model.addAttribute("categories", categoryService.findAllActiveCategories());
            model.addAttribute("origins", originService.findAllActiveOrigins());
        } catch (Exception e) {
            System.err.println("Error fetching categories/origins for filtering: " + e.getMessage());
            model.addAttribute("categories", Collections.emptyList());
            model.addAttribute("origins", Collections.emptyList());
        }

        model.addAttribute("pageMetadata", breadcrumbService.buildAdminOriginDetailPageMetadata());

        return "client/pages/product/list";
    }

}
