package com.vn.fruitcart.controller.admin;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vn.fruitcart.entity.Category;
import com.vn.fruitcart.entity.Product;
import com.vn.fruitcart.entity.dto.request.category.CategoryReq;
import com.vn.fruitcart.entity.dto.request.category.CategorySearchCriteria;
import com.vn.fruitcart.exception.ResourceNotFoundException;
import com.vn.fruitcart.service.BreadcrumbService;
import com.vn.fruitcart.service.CategoryService;
import com.vn.fruitcart.service.ProductService;
import com.vn.fruitcart.util.FruitCartUtils;

import groovyjarjarpicocli.CommandLine.DuplicateNameException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {
    private final CategoryService categoryService;
    private final BreadcrumbService breadcrumbService;
    private final ProductService productService;

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
            @PageableDefault(size = 5, sort = "id", direction = Direction.DESC) Pageable pageable,
            @ModelAttribute("criteria") CategorySearchCriteria criteria) {
        model.addAttribute("pageMetadata", breadcrumbService.buildAdminCategoryPageMetadata());

        Page<Category> categoryPage = categoryService.findUsersByCriteria(criteria, pageable);

        model.addAttribute("categoryPage", categoryPage);

        FruitCartUtils.addPagingAndSortingAttributes(model, pageable);

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

    @PostMapping("/delete/{categoryId}")
    public String deleteCategory(@PathVariable("categoryId") Long id, RedirectAttributes redirectAttributes) {
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
            @PageableDefault(size = 5, sort = "name") Pageable pageable,
            RedirectAttributes redirectAttributes) {
        model.addAttribute("pageMetadata", breadcrumbService.buildAdminCategoryDetailPageMetadata());

        try {
            Category category = categoryService.getCategoryById(id);
            model.addAttribute("category", category);
            Page<Product> productPage = productService.findPaginatedByCategoryId(id, pageable);
            model.addAttribute("productPage", productPage);
            return "admin/pages/category/detail";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy danh mục: " + e.getMessage());
            return "redirect:/admin/categories";
        }
    }
}
