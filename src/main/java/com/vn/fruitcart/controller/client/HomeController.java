package com.vn.fruitcart.controller.client;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.vn.fruitcart.entity.Category;
import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.entity.dto.BestSellingProductDto;
import com.vn.fruitcart.entity.dto.request.product.ProductSearchCriteria;
import com.vn.fruitcart.entity.dto.response.PageMetadata;
import com.vn.fruitcart.service.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import com.vn.fruitcart.entity.Product;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final OriginService originService;
    private final BreadcrumbService breadcrumbService;
    private final UserService userService;

    @GetMapping("/")
    public String getHomePage(Model model) {
        List<Category> featuredCategories = categoryService.findAllActiveCategories()
                .stream()
                .limit(5)
                .toList();

        boolean hideBreadcrumb = true;
        model.addAttribute("hideBreadcrumb", hideBreadcrumb);
        return "client/pages/index";
    }

    @GetMapping("/san-pham")
    public String index(
            Model model,
            @ModelAttribute("criteria") ProductSearchCriteria criteria,
            @PageableDefault(size = 8, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {

        Sort newProductSort = Sort.by(Sort.Direction.DESC, "isNew");

        Sort userSort = pageable.getSort();

        Sort finalSort = newProductSort.and(userSort);

        Pageable finalPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), finalSort);

        Page<Product> productPage = productService.searchProducts(criteria, finalPageable);

        String currentSort = pageable.getSort().get()
                .map(order -> order.getProperty() + "," + order.getDirection().name().toLowerCase())
                .collect(Collectors.joining());

        model.addAttribute("currentSort", currentSort);

        User currentUser = null;
        try {
            currentUser = userService.getCurrentUser();
        } catch (Exception ignored) {

        }
        model.addAttribute("currentUser", currentUser);

        model.addAttribute("productPage", productPage);
        model.addAttribute("categories", categoryService.findAllActiveCategories());
        model.addAttribute("origins", originService.findAllActiveOrigins());
        model.addAttribute("pageMetadata", breadcrumbService.demo());

        model.addAttribute("productPage", productPage);
        model.addAttribute("categories", categoryService.findAllActiveCategories());
        model.addAttribute("origins", originService.findAllActiveOrigins());

        model.addAttribute("pageMetadata", breadcrumbService.demo());

        return "client/pages/product/list";
    }

    @GetMapping("/about-us")
    public String getAboutUsPage(Model model) {
        model.addAttribute("pageMetadata", breadcrumbService.demo());
        return "client/pages/about-us";
    }

    @GetMapping("/contact")
    public String getContactPage(Model model) {
        model.addAttribute("pageMetadata", breadcrumbService.demo());
        return "client/pages/contact";
    }

    @GetMapping("/gio-hang")
    public String cartPage(Model model) {
        model.addAttribute("pageMetadata", breadcrumbService.buildCartDetail());

        return "client/pages/cart/detail";
    }

}
