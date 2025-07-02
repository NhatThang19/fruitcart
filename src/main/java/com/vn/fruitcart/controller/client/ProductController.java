package com.vn.fruitcart.controller.client;

import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.service.CategoryService;
import com.vn.fruitcart.service.UserService;
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
@RequestMapping("/san-pham")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final BreadcrumbService breadcrumbService;
    private final UserService userService;

    @GetMapping("/{slug}")
    public String getProductDetailPage(@PathVariable String slug, Model model) {
        try {
            model.addAttribute("pageMetadata", breadcrumbService.buildClientProductDetail());
            Product product = productService.getProductBySlug(slug); 
            model.addAttribute("product", product);
            model.addAttribute("pageTitle", product.getName());

            User currentUser = userService.getCurrentUser();
            model.addAttribute("currentUser", currentUser);


            return "client/pages/product/detail";

        } catch (ResourceNotFoundException e) {
            return "error/404";
        }
    }
}
