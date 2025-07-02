package com.vn.fruitcart.controller.admin;

import java.util.ArrayList;
import java.util.List;

import com.vn.fruitcart.entity.Product;
import com.vn.fruitcart.entity.dto.BestSellingProductDto;
import com.vn.fruitcart.entity.dto.SalesDataDto;
import com.vn.fruitcart.repository.OrderRepository;
import com.vn.fruitcart.service.BreadcrumbService;
import com.vn.fruitcart.service.OrderService;
import com.vn.fruitcart.service.ProductService;
import com.vn.fruitcart.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.vn.fruitcart.entity.dto.response.PageMetadata;

@Controller
@RequiredArgsConstructor
public class DashboardController {
    private final ProductService productService;
    private final OrderService orderService;
    private final UserService userService;
    private final BreadcrumbService breadcrumbService;

    @GetMapping("/admin")
    public String getDashboardPage(Model model) {
        List<Product> lowStockProducts = productService.findLowStockProducts(5);
        model.addAttribute("lowStockProducts", lowStockProducts);

        List<BestSellingProductDto> bestSellingProducts = productService.findTopBestSellingProducts(5);
        model.addAttribute("bestSellingProducts", bestSellingProducts);

        List<Product> onSaleProducts = productService.findOnSaleProducts();
        model.addAttribute("onSaleProducts", onSaleProducts);

        List<SalesDataDto> salesData = orderService.getMonthlySalesDataLastYear();
        model.addAttribute("salesData", salesData);

        model.addAttribute("totalOrders", orderService.countTotalOrders());
        model.addAttribute("totalRevenue", orderService.getTotalRevenue());
        model.addAttribute("totalUsers", userService.countTotalUsers());

        model.addAttribute("userClusterCounts", userService.getUserCountByCluster());

        model.addAttribute("recentOrders", orderService.findRecentOrders());


        // Page Metadata
        List<PageMetadata.BreadcrumbSegment> segments = new ArrayList<>();
        segments.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
        PageMetadata pageMetadata = new PageMetadata("Dashboard", segments);
        model.addAttribute("pageMetadata", pageMetadata);
        model.addAttribute("isDashboard", true);

        return "admin/pages/dashboard";
    }
}


