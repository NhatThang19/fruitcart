package com.vn.fruitcart.controller.admin;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vn.fruitcart.entity.Category;
import com.vn.fruitcart.entity.dto.request.CategoryReq;
import com.vn.fruitcart.exception.ResourceNotFoundException;
import com.vn.fruitcart.service.BreadcrumbService;
import com.vn.fruitcart.service.CategoryService;

import groovyjarjarpicocli.CommandLine.DuplicateNameException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {
    private final CategoryService categoryService;
    private final BreadcrumbService breadcrumbService;

    @GetMapping("/create")
    public String showAddCategoryForm(Model model) {
        model.addAttribute("pageMetadata", breadcrumbService.buildAdminCategoryCreatePageMetadata());
        model.addAttribute("categoryReq", new CategoryReq());

        return "admin/pages/category/create";
    }

    @PostMapping("/create")
    public String addCategory(@Valid @ModelAttribute("categoryReq") CategoryReq categoryReq,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        model.addAttribute("pageMetadata", breadcrumbService.buildAdminCategoryCreatePageMetadata());
        if (bindingResult.hasErrors()) {
            return "admin/pages/category/create";
        }
        try {
            categoryService.createCategory(categoryReq);
            redirectAttributes.addFlashAttribute("message", "Thêm danh mục thành công.");
            redirectAttributes.addFlashAttribute("messageType", "success");
            redirectAttributes.addFlashAttribute("successMessage",
                    "Thêm danh mục '" + categoryReq.getName() + "'' thành công");
            return "redirect:/admin/categories";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm danh mục: " + e.getMessage());
            return "redirect:/admin/categories/create";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm danh mục: " + e.getMessage());
            return "redirect:/admin/categories/create";
        }
    }

    @GetMapping
    public String listCategories(Model model,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        model.addAttribute("pageMetadata", breadcrumbService.buildAdminCategoryPageMetadata());

        String sortField = sort[0];
        String sortDir = (sort.length > 1 && (sort[1].equalsIgnoreCase("asc") || sort[1].equalsIgnoreCase("desc")))
                ? sort[1]
                : "asc";
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        Boolean statusFilter = null;
        if (status != null && !status.isEmpty()) {
            if ("true".equalsIgnoreCase(status)) {
                statusFilter = true;
            } else if ("false".equalsIgnoreCase(status)) {
                statusFilter = false;
            }
        }
        model.addAttribute("selectedStatus", status == null ? "" : status);

        Page<Category> categoryPage = categoryService.getAllCategoriesWithFiltersAndPagination(keyword, statusFilter,
                page, size, sortField, sortDir);

        model.addAttribute("categories", categoryPage.getContent());
        model.addAttribute("currentPage", categoryPage.getNumber() + 1);
        model.addAttribute("totalPages", categoryPage.getTotalPages());
        model.addAttribute("totalItems", categoryPage.getTotalElements());
        model.addAttribute("pageSize", size);

        if (categoryPage.getTotalPages() > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, categoryPage.getTotalPages())
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        model.addAttribute("keyword", keyword);

        return "admin/pages/category/list";
    }

    @GetMapping("/update/{id}")
    public String showEditCategoryForm(@PathVariable("id") Long id, Model model,
            RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("pageMetadata", breadcrumbService.buildAdminCategoryUpdatePageMetadata());

            Category category = categoryService.getCategoryById(id);
            CategoryReq categoryReq = new CategoryReq();
            categoryReq.setName(category.getName());
            categoryReq.setDescription(category.getDescription());
            categoryReq.setStatus(category.getStatus());

            model.addAttribute("categoryReq", categoryReq);
            model.addAttribute("categoryId", id);

            return "admin/pages/category/update";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/categories";
        }
    }

    @PostMapping("/update/{id}")
    public String processUpdateCategory(@PathVariable("id") Long id,
            @Valid @ModelAttribute("categoryReq") CategoryReq categoryReq,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        model.addAttribute("pageMetadata", breadcrumbService.buildAdminCategoryUpdatePageMetadata());
        if (bindingResult.hasErrors()) {
            model.addAttribute("categoryId", id);
            return "admin/pages/category/update";
        }

        try {
            categoryService.updateCategory(id, categoryReq);
            redirectAttributes.addFlashAttribute("message", "Cập nhật danh mục thành công.");
            redirectAttributes.addFlashAttribute("messageType", "success");
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cập nhật danh mục '" + categoryReq.getName() + "' thành công.");
            return "redirect:/admin/categories";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/categories";
        } catch (DuplicateNameException e) {
            bindingResult.rejectValue("name", "error.categoryReq", e.getMessage());
            model.addAttribute("categoryId", id);
            return "admin/pages/category/update";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật danh mục: " + e.getMessage());
            return "redirect:/admin/categories/update/" + id;
        }
    }

    @PostMapping("/delete")
    public String deleteCategory(@RequestParam("categoryId") Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("message", "Xoá danh mục thành công.");
            redirectAttributes.addFlashAttribute("messageType", "success");
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa danh mục thành công.");
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi không mong muốn khi xóa danh mục.");
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/detail/{id}")
    public String showCategoryDetail(@PathVariable("id") Long id, Model model,
            RedirectAttributes redirectAttributes) {
        model.addAttribute("pageMetadata", breadcrumbService.buildAdminCategoryDetailPageMetadata());
        try {
            Category category = categoryService.getCategoryById(id);
            model.addAttribute("category", category);
            return "admin/pages/category/detail";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy danh mục: " + e.getMessage());
            return "redirect:/admin/categories";
        }
    }
}
