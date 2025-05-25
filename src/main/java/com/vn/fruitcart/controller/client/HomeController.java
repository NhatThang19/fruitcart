package com.vn.fruitcart.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String getHomePage(Model model) {
        boolean hideBreadcrumb = true;
        model.addAttribute("hideBreadcrumb", hideBreadcrumb);
        return "client/pages/index";
    }

}
