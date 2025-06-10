package com.vn.fruitcart.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.vn.fruitcart.entity.Product;
import com.vn.fruitcart.exception.ResourceNotFoundException;
import com.vn.fruitcart.service.BreadcrumbService;
import com.vn.fruitcart.service.ProductService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final BreadcrumbService breadcrumbService;

    @GetMapping("/{id}")
    public String getProductDetailPage(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("pageMetadata", breadcrumbService.buildAdminOriginDetailPageMetadata());
            Product product = productService.getProductById(id);
            model.addAttribute("product", product);

            return "client/pages/product/detail";
        } catch (ResourceNotFoundException e) {
            return "error/404"; 
        }
    }
}
