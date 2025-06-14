package com.vn.fruitcart.controller.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vn.fruitcart.entity.Category;
import com.vn.fruitcart.entity.Origin;
import com.vn.fruitcart.entity.Product;
import com.vn.fruitcart.entity.ProductImage;
import com.vn.fruitcart.entity.dto.request.ProductUpdateReq;
import com.vn.fruitcart.entity.dto.request.ProductVariantUpdateReq;
import com.vn.fruitcart.entity.dto.request.product.ProductCreateReq;
import com.vn.fruitcart.entity.dto.request.product.ProductSearchCriteriaReq;
import com.vn.fruitcart.exception.ResourceNotFoundException;
import com.vn.fruitcart.service.BreadcrumbService;
import com.vn.fruitcart.service.CategoryService;
import com.vn.fruitcart.service.OriginService;
import com.vn.fruitcart.service.ProductService;
import com.vn.fruitcart.util.FruitCartUtils;

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
        model.addAttribute("pageMetadata", breadcrumbService.buildAdminProductCreate());
        model.addAttribute("productDTO", new ProductCreateReq());

        try {
            List<Category> categories = categoryService.findAllActiveCategories();
            List<Origin> origins = originService.findAllActiveOrigins();

            model.addAttribute("categories", categories);
            model.addAttribute("origins", origins);
        } catch (Exception e) {
            model.addAttribute("pageErrorMessage", "Không thể tải dữ liệu cần thiết cho form (danh mục/nguồn gốc).");
        }

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

        model.addAttribute("pageMetadata", breadcrumbService.buildAdminProductCreate());

        if (bindingResult.hasErrors()) {
            try {
                List<Category> categories = categoryService.findAllActiveCategories();
                model.addAttribute("categories", categories);

                List<Origin> origins = originService.findAllActiveOrigins();
                model.addAttribute("origins", origins);
            } catch (Exception e) {
                model.addAttribute("pageErrorMessage", "Lỗi tải lại dữ liệu phụ trợ cho form.");
            }

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
    public String listProducts(Model model,
            @PageableDefault(size = 5, sort = "id", direction = Direction.DESC) Pageable pageable,
            @ModelAttribute("criteria") ProductSearchCriteriaReq criteria) {
        model.addAttribute("pageMetadata", breadcrumbService.buildAdminProduct());

        List<Category> categories = categoryService.getAllCategories();
        List<Origin> origins = originService.getAllOrigins();

        model.addAttribute("categories", categories);
        model.addAttribute("origins", origins);

        Page<Product> productsPage = productService.findProductsByCriteria(criteria, pageable);

        model.addAttribute("productsPage", productsPage);

        FruitCartUtils.addPagingAndSortingAttributes(model, pageable);

        return "admin/pages/product/list";
    }

    @PostMapping("/delete/{productId}")
    public String deleteProductByForm(
            @PathVariable("productId") Long id,
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

            model.addAttribute("pageMetadata", breadcrumbService.buildAdminDetailProduct());

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
