package com.vn.fruitcart.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import com.vn.fruitcart.entity.Category;
import com.vn.fruitcart.entity.dto.response.PageMetadata;
import com.vn.fruitcart.service.CategoryService;
import com.vn.fruitcart.util.SlugUtil;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/admin/categories")
    public String getAdminCategoryPage(Model model) {
        List<PageMetadata.BreadcrumbSegment> segments = new ArrayList<>();
        segments.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
        segments.add(new PageMetadata.BreadcrumbSegment("Danh mục", "/admin/categories"));

        PageMetadata pageMetadata = new PageMetadata("Danh mục", segments);
        model.addAttribute("pageMetadata", pageMetadata);

        model.addAttribute("category", new Category());
        return "admin/pages/categories/view";
    }

    @PostMapping("/admin/categories/create")
    public String handleCreateCategory(
            @Valid @ModelAttribute() Category category, BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            List<PageMetadata.BreadcrumbSegment> segments = new ArrayList<>();
            segments.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
            segments.add(new PageMetadata.BreadcrumbSegment("Danh mục", "/admin/categories"));

            PageMetadata pageMetadata = new PageMetadata("Danh mục", segments);
            model.addAttribute("pageMetadata", pageMetadata);

            model.addAttribute("category", category);
            return "admin/pages/categories/view";
        }

        String slug = SlugUtil.toSlug(category.getName());
        category.setSlug(slug);

        slug = categoryService.generateUniqueSlug(slug);
        category.setSlug(slug);

        categoryService.save(category);

        redirectAttributes.addFlashAttribute("message", "Thêm danh mục thành công!");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/admin/categories";
    }

}
