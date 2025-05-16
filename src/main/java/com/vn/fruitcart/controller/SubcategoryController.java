package com.vn.fruitcart.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vn.fruitcart.entity.Category;
import com.vn.fruitcart.entity.SubCategory;
import com.vn.fruitcart.entity.dto.response.PageMetadata;
import com.vn.fruitcart.service.CategoryService;
import com.vn.fruitcart.service.SubCategoryService;
import com.vn.fruitcart.util.StringUtil;

import jakarta.validation.Valid;

@Controller
public class SubcategoryController {
    private final SubCategoryService subCategoryService;
    private final CategoryService categoryService;

    public SubcategoryController(SubCategoryService subCategoryService, CategoryService categoryService) {
        this.subCategoryService = subCategoryService;
        this.categoryService = categoryService;
    }

    @GetMapping("/admin/subcategories")
    public String getSubcategoriesPage(Model model) {
        List<PageMetadata.BreadcrumbSegment> segments = new ArrayList<>();
        segments.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
        segments.add(new PageMetadata.BreadcrumbSegment("Danh mục con", "/admin/subcategories"));

        PageMetadata pageMetadata = new PageMetadata("Danh mục con", segments);
        model.addAttribute("pageMetadata", pageMetadata);

        List<Category> parentCategories = categoryService.findAllParentCategories();
        model.addAttribute("parentCategories", parentCategories);

        model.addAttribute("subcategory", new SubCategory());

        return "admin/pages/subcategories/view";
    }

    @PostMapping("/admin/subcategories/create") 
    public String handleCreateSubcategory(
            @Valid @ModelAttribute SubCategory subcategory,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            List<PageMetadata.BreadcrumbSegment> segments = new ArrayList<>();
            segments.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
            segments.add(new PageMetadata.BreadcrumbSegment("Danh mục con", "/admin/subcategories"));

            PageMetadata pageMetadata = new PageMetadata("Danh mục con", segments);
            model.addAttribute("pageMetadata", pageMetadata);

            List<Category> parentCategories = categoryService.findAllParentCategories();
            model.addAttribute("parentCategories", parentCategories);

            return "admin/pages/subcategories/view";
        }

        String slug = StringUtil.toSlug(subcategory.getName());
        subcategory.setSlug(slug);
        
        subCategoryService.save(subcategory);

        redirectAttributes.addFlashAttribute("message", "Thêm danh mục con thành công!");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/admin/subcategories";
    }

    @GetMapping("/admin/subcategories/edit")
    public String showEditSubcategoryForm(@RequestParam Long id, Model model, RedirectAttributes redirectAttributes) {
        SubCategory subcategory = this.subCategoryService.getSubCategoryById(id);
        if (subcategory == null) {
            redirectAttributes.addFlashAttribute("message", "Không tìm thấy danh mục con!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/admin/subcategories";
        }

        List<PageMetadata.BreadcrumbSegment> segments = new ArrayList<>();
        segments.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
        segments.add(new PageMetadata.BreadcrumbSegment("Danh mục con", "/admin/subcategories"));
        segments.add(new PageMetadata.BreadcrumbSegment("Sửa danh mục con", "/admin/subcategories/edit"));

        PageMetadata pageMetadata = new PageMetadata("Sửa danh mục con", segments);
        model.addAttribute("pageMetadata", pageMetadata);

        List<Category> parentCategories = categoryService.findAllParentCategories();
        model.addAttribute("parentCategories", parentCategories);
        model.addAttribute("subcategory", subcategory);

        return "admin/pages/subcategories/edit";
    }

    @PostMapping("/admin/subcategories/edit")
    public String handleEditSubcategory(
            @RequestParam Long id,
            @Valid @ModelAttribute SubCategory subcategory,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            List<PageMetadata.BreadcrumbSegment> segments = new ArrayList<>();
            segments.add(new PageMetadata.BreadcrumbSegment("Dashboard", "/admin"));
            segments.add(new PageMetadata.BreadcrumbSegment("Danh mục con", "/admin/subcategories"));
            segments.add(new PageMetadata.BreadcrumbSegment("Sửa danh mục con", "/admin/subcategories/edit"));

            PageMetadata pageMetadata = new PageMetadata("Sửa danh mục con", segments);
            model.addAttribute("pageMetadata", pageMetadata);

            List<Category> parentCategories = categoryService.findAllParentCategories();
            model.addAttribute("parentCategories", parentCategories);

            return "admin/pages/subcategories/edit";
        }

        SubCategory existingSubcategory = subCategoryService.getSubCategoryById(id);
        if (existingSubcategory == null) {
            redirectAttributes.addFlashAttribute("message", "Không tìm thấy danh mục con!");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/admin/subcategories";
        }

        if (!existingSubcategory.getName().equals(subcategory.getName())) {
            String slug = StringUtil.toSlug(subcategory.getName());
            subcategory.setSlug(slug);
        } else {
            subcategory.setSlug(existingSubcategory.getSlug());
        }

        subcategory.setId(id);
        subCategoryService.save(subcategory);

        redirectAttributes.addFlashAttribute("message", "Cập nhật danh mục con thành công!");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/admin/subcategories";
    }

    @GetMapping("/admin/subcategories/delete")
    public String deleteSubcategory(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        try {
            SubCategory subcategory = this.subCategoryService.getSubCategoryById(id);
            if (subcategory != null) {
                this.subCategoryService.deleteById(id);
                redirectAttributes.addFlashAttribute("message", "Xoá danh mục con thành công!");
                redirectAttributes.addFlashAttribute("messageType", "success");
            } else {
                redirectAttributes.addFlashAttribute("message", "Không tìm thấy danh mục con!");
                redirectAttributes.addFlashAttribute("messageType", "error");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Xoá danh mục con thất bại!");
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        return "redirect:/admin/subcategories";
    }
}
