package com.vn.fruitcart.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import com.vn.fruitcart.entity.Category;
import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.entity.dto.response.PageMetadata;
import com.vn.fruitcart.service.CategoryService;
import com.vn.fruitcart.util.SlugUtil;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/admin/categories/delete")
    public String deleteCategory(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        try {
            Category category = this.categoryService.getCategoryById(id);
            if (category != null) {
                this.categoryService.deleteById(id);
                redirectAttributes.addFlashAttribute("message", "Xoá danh mục thành công!");
                redirectAttributes.addFlashAttribute("messageType", "success");
            } else {
                redirectAttributes.addFlashAttribute("message", "Không tìm thấy danh mục!");
                redirectAttributes.addFlashAttribute("messageType", "error");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Xoá danh mục thất bại!");
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/admin/categories/edit")
    public String showEditCategoryForm(@RequestParam Long id, Model model, RedirectAttributes redirectAttributes) {
        Category category = this.categoryService.getCategoryById(id);
        if (category == null) {
            redirectAttributes.addFlashAttribute("message", "Không tìm thấy danh mục!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/admin/categories";
        }

        List<PageMetadata.BreadcrumbSegment> segments = new ArrayList<>();
        segments.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
        segments.add(new PageMetadata.BreadcrumbSegment("Danh mục", "/admin/categories"));
        segments.add(new PageMetadata.BreadcrumbSegment("Sửa danh mục", "/admin/categories/edit"));

        PageMetadata pageMetadata = new PageMetadata("Sửa danh mục", segments);
        model.addAttribute("pageMetadata", pageMetadata);
        model.addAttribute("category", category);
        
        return "admin/pages/categories/edit";
    }

    @PostMapping("/admin/categories/edit")
    public String handleEditCategory(
            @RequestParam Long id,
            @Valid @ModelAttribute Category category,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            List<PageMetadata.BreadcrumbSegment> segments = new ArrayList<>();
            segments.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
            segments.add(new PageMetadata.BreadcrumbSegment("Danh mục", "/admin/categories"));
            segments.add(new PageMetadata.BreadcrumbSegment("Sửa danh mục", "/admin/categories/edit"));

            PageMetadata pageMetadata = new PageMetadata("Sửa danh mục", segments);
            model.addAttribute("pageMetadata", pageMetadata);
            return "admin/pages/categories/edit";
        }

        Category existingCategory = categoryService.getCategoryById(id);
        if (existingCategory == null) {
            redirectAttributes.addFlashAttribute("message", "Không tìm thấy danh mục!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/admin/categories";
        }

        // Update only if name has changed
        if (!existingCategory.getName().equals(category.getName())) {
            String slug = SlugUtil.toSlug(category.getName());
            category.setSlug(slug);
            
            slug = categoryService.generateUniqueSlug(slug);
            category.setSlug(slug);
        } else {
            category.setSlug(existingCategory.getSlug());
        }

        category.setId(id); // Ensure we're updating the existing category
        categoryService.save(category);

        redirectAttributes.addFlashAttribute("message", "Cập nhật danh mục thành công!");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/admin/categories";
    }

}
