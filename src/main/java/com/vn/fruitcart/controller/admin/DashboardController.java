package com.vn.fruitcart.controller.admin;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.vn.fruitcart.entity.dto.response.PageMetadata;

@Controller
public class DashboardController {
    @GetMapping("/admin")
    public String getDashboardPage(Model model) {
        List<PageMetadata.BreadcrumbSegment> segments = new ArrayList<>();
        segments.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
        PageMetadata pageMetadata = new PageMetadata("Dashboard", segments);
        model.addAttribute("pageMetadata", pageMetadata);
        model.addAttribute("isDashboard", true);
        return "admin/pages/dashboard";
    }

}
