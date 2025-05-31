package com.vn.fruitcart.controller.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vn.fruitcart.entity.Category;
import com.vn.fruitcart.entity.Origin;
import com.vn.fruitcart.entity.Product;
import com.vn.fruitcart.entity.dto.request.ProductCreateReq;
import com.vn.fruitcart.service.BreadcrumbService;
import com.vn.fruitcart.service.CategoryService;
import com.vn.fruitcart.service.OriginService;
import com.vn.fruitcart.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final OriginService originService;
    private final BreadcrumbService breadcrumbService;

    @GetMapping("/create")
    public String showCreateProductForm(Model model) {
        model.addAttribute("productDTO", new ProductCreateReq());

        try {
            List<Category> categories = categoryService.findAllActiveCategories();
            model.addAttribute("categories", categories);

            List<Origin> origins = originService.findAllActiveOrigins();
            model.addAttribute("origins", origins);
        } catch (Exception e) {
            model.addAttribute("pageErrorMessage", "Không thể tải dữ liệu cần thiết cho form (danh mục/nguồn gốc).");
        }

        model.addAttribute("pageMetadata", breadcrumbService.buildAdminOriginDetailPageMetadata());
        return "admin/pages/product/create";
    }

    @PostMapping("/create")
    public String createProduct(
            @Valid @ModelAttribute("productDTO") ProductCreateReq productDTO,
            BindingResult bindingResult,
            @RequestParam(name = "images", required = false) List<MultipartFile> images,
            @RequestParam(name = "isMainImage", required = false) List<String> mainImageFlags,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            try {
                List<Category> categories = categoryService.findAllActiveCategories();
                model.addAttribute("categories", categories);

                List<Origin> origins = originService.findAllActiveOrigins();
                model.addAttribute("origins", origins);
            } catch (Exception e) {
                model.addAttribute("pageErrorMessage", "Lỗi tải lại dữ liệu phụ trợ cho form.");
            }

            model.addAttribute("pageMetadata", breadcrumbService.buildAdminOriginDetailPageMetadata());
            return "admin/pages/product/create";
        }

        List<Boolean> isMainImages = new ArrayList<>();
        if (images != null && !images.isEmpty() && mainImageFlags != null && mainImageFlags.size() == images.size()) {

            for (String flag : mainImageFlags) {
                isMainImages.add("true".equalsIgnoreCase(flag));
            }
        } else if (images != null && !images.isEmpty()) {
            for (int i = 0; i < images.size(); i++) {
                isMainImages.add(false);
            }

        }

        try {
            List<MultipartFile> actualImages = images != null ? images.stream()
                    .filter(img -> img != null && !img.isEmpty())
                    .collect(Collectors.toList())
                    : new ArrayList<>();

            Product createdProduct = productService.createProduct(productDTO, actualImages, isMainImages);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Sản phẩm '" + createdProduct.getName() + "' đã được tạo thành công!");

            return "redirect:/admin/products";
        } catch (Exception e) {

            model.addAttribute("errorMessage", "Tạo sản phẩm thất bại: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Tạo sản phẩm thất bại: " + e.getMessage());
            try {
                List<Category> categories = categoryService.findAllActiveCategories();
                model.addAttribute("categories", categories);

                List<Origin> origins = originService.findAllActiveOrigins();
                model.addAttribute("origins", origins);
            } catch (Exception loadEx) {

                model.addAttribute("pageErrorMessage", "Lỗi tải lại dữ liệu phụ trợ cho form.");
            }
            model.addAttribute("productDTO", productDTO);

            model.addAttribute("pageMetadata", breadcrumbService.buildAdminOriginDetailPageMetadata());
            return "admin/pages/product/create";
        }
    }

}
