package com.vn.fruitcart.controller.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vn.fruitcart.entity.Category;
import com.vn.fruitcart.entity.Origin;
import com.vn.fruitcart.entity.Product;
import com.vn.fruitcart.entity.ProductImage;
import com.vn.fruitcart.entity.dto.request.ProductCreateReq;
import com.vn.fruitcart.entity.dto.request.ProductUpdateReq;
import com.vn.fruitcart.entity.dto.request.ProductVariantUpdateReq;
import com.vn.fruitcart.exception.ResourceNotFoundException;
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

    @GetMapping("/edit/{id}")
    public String showEditProductForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.getProductById(id);

            ProductUpdateReq productDTO = new ProductUpdateReq();
            productDTO.setId(product.getId());
            productDTO.setName(product.getName());
            productDTO.setDescription(product.getDescription());
            productDTO.setPrice(product.getBasePrice());
            if (product.getCategory() != null) {
                productDTO.setCategoryId(product.getCategory().getId());
            }
            if (product.getOrigin() != null) {
                productDTO.setOriginId(product.getOrigin().getId());
            }
            productDTO.setNew(product.isNew());
            productDTO.setStatus(product.isStatus());

            if (product.getVariants() != null) {
                List<ProductVariantUpdateReq> variantDTOs = product.getVariants().stream()
                        .map(v -> {
                            ProductVariantUpdateReq vd = new ProductVariantUpdateReq();
                            vd.setId(v.getId());
                            vd.setAttribute(v.getAttribute());
                            vd.setSku(v.getSku());
                            vd.setPrice(v.getPrice());
                            return vd;
                        }).collect(Collectors.toList());
                productDTO.setVariants(variantDTOs);
            }

            product.getImages().stream()
                    .filter(ProductImage::isMain)
                    .findFirst()
                    .ifPresent(mainImg -> productDTO.setMainImageId(mainImg.getId()));

            model.addAttribute("productDTO", productDTO);
            model.addAttribute("existingImages", product.getImages());

            model.addAttribute("categories", categoryService.findAllActiveCategories());
            model.addAttribute("origins", originService.findAllActiveOrigins());

            model.addAttribute("pageMetadata", breadcrumbService.buildAdminOriginDetailPageMetadata());
            return "admin/pages/product/edit";

        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi tải trang chỉnh sửa sản phẩm.");
            return "redirect:/admin/products";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("productDTO") ProductUpdateReq productDTO,
            BindingResult bindingResult,
            @RequestParam(name = "newImages", required = false) List<MultipartFile> newImages,
            @RequestParam(name = "imageIdsToDelete", required = false) List<Long> imageIdsToDelete,
            RedirectAttributes redirectAttributes,
            Model model) {
        model.addAttribute("pageMetadata", breadcrumbService.buildAdminOriginDetailPageMetadata());

        productDTO.setId(id);

        if (bindingResult.hasErrors()) {
            try {
                if (productDTO.getId() != null) {
                    Product productForForm = productService.getProductById(productDTO.getId());
                    model.addAttribute("existingImages", productForForm.getImages());
                }
                model.addAttribute("categories", categoryService.findAllActiveCategories());
                model.addAttribute("origins", originService.findAllActiveOrigins());
            } catch (Exception e) {
                model.addAttribute("pageErrorMessage", "Lỗi tải lại dữ liệu phụ trợ cho form.");
            }
            model.addAttribute("pageMetadata", breadcrumbService.buildAdminOriginDetailPageMetadata());
            return "admin/pages/product/edit";
        }

        try {
            List<MultipartFile> actualNewImages = newImages != null ? newImages.stream()
                    .filter(img -> img != null && !img.isEmpty())
                    .collect(Collectors.toList())
                    : new ArrayList<>();

            Product updatedProduct = productService.updateProduct(id, productDTO, actualNewImages, imageIdsToDelete);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Sản phẩm '" + updatedProduct.getName() + "' đã được cập nhật thành công!");

            return "redirect:/admin/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cập nhật sản phẩm thất bại: " + e.getMessage());
            return "redirect:/admin/products/edit/" + id;
        }
    }

    @GetMapping
    public String listProducts(
            @RequestParam(name = "page", defaultValue = "1") int pageParam, 
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "originId", required = false) Long originId,
            @RequestParam(name = "status", required = false) Boolean status,
            @RequestParam(name = "sortField", defaultValue = "id") String sortField,
            @RequestParam(name = "sortDir", defaultValue = "desc") String sortDir,
            Model model) {
        model.addAttribute("pageMetadata", breadcrumbService.buildAdminOriginDetailPageMetadata());

        Pageable pageable = PageRequest.of(pageParam - 1, size); 
        Page<Product> productPage; 

        List<Category> categories = new ArrayList<>();
        List<Origin> origins = new ArrayList<>();

        try {
            productPage = productService.findAllAdminProducts(pageable, keyword, categoryId, originId, status,
                    sortField, sortDir);

            categories = categoryService.findAllActiveCategories(); 
            origins = originService.findAllActiveOrigins(); 

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Không thể tải danh sách sản phẩm. Vui lòng thử lại sau.");
            productPage = Page.empty(pageable);

        }


        model.addAttribute("productPage", productPage);
        model.addAttribute("categories", categories);
        model.addAttribute("origins", origins);


        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedOriginId", originId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", "asc".equals(sortDir) ? "desc" : "asc");


        int totalPages = productPage.getTotalPages();
        List<Integer> pageNumbers = new ArrayList<>();
        if (totalPages > 0) {
            int currentPageNumberInView = productPage.getNumber() + 1; 
            int startPage = Math.max(1, currentPageNumberInView - 2);
            int endPage = Math.min(totalPages, currentPageNumberInView + 2);

            if (currentPageNumberInView <= 3) {
                endPage = Math.min(totalPages, 5);
            }
            if (currentPageNumberInView >= totalPages - 2) {
                startPage = Math.max(1, totalPages - 4);
            }

            for (int i = startPage; i <= endPage; i++) {
                pageNumbers.add(i);
            }
        }
        model.addAttribute("pageNumbers", pageNumbers);
        return "admin/pages/product/list";
    }

    @PostMapping("/delete") 
    public String deleteProductByForm(
            @RequestParam("productId") Long id,
            RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("successMessage", "Sản phẩm ID " + id + " đã được xóa thành công!");
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Xóa sản phẩm thất bại: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/detail/{id}")
    public String showProductDetail(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.getProductById(id);
            model.addAttribute("product", product);

            model.addAttribute("pageMetadata", breadcrumbService.buildAdminOriginDetailPageMetadata());

            return "admin/pages/product/detail";

        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi tải trang chi tiết sản phẩm.");
            return "redirect:/admin/products";
        }
    }

}
