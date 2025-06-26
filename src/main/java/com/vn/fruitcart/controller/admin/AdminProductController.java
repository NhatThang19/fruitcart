package com.vn.fruitcart.controller.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.vn.fruitcart.util.mapper.ProductMapper;
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
import com.vn.fruitcart.entity.dto.request.product.ProductUpdateReq;
import com.vn.fruitcart.entity.dto.request.product.ProductVariantUpdateReq;
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
    private final ProductMapper productMapper;

    @GetMapping("/create")
    public String showCreateProductForm(Model model) {
        try {
            model.addAttribute("pageMetadata", breadcrumbService.buildAdminProductCreate());
            model.addAttribute("productCreateReq", new ProductCreateReq());

            List<Category> categories = categoryService.findAllActiveCategories();
            List<Origin> origins = originService.findAllActiveOrigins();

            model.addAttribute("categories", categories);
            model.addAttribute("origins", origins);
        } catch (Exception e) {
            model.addAttribute("pageErrorMessage", "Có lỗ xảy ra khi chuẩn bị dữ liệu.");
        }

        return "admin/pages/product/create";
    }

    @PostMapping("/create")
    public String createProduct(@Valid @ModelAttribute("productCreateReq") ProductCreateReq productCreateReq,
                                BindingResult bindingResult,
                                @RequestParam(name = "images", required = false) List<MultipartFile> images,
                                @RequestParam(name = "isMainImage", required = false) List<String> mainImageFlags,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        model.addAttribute("pageMetadata", breadcrumbService.buildAdminProductCreate());

        if (bindingResult.hasErrors()) {
            loadFormAuxiliaryData(model);
            model.addAttribute("productCreateReq", productCreateReq);
            return "admin/pages/product/create";
        }

        try {
            List<MultipartFile> actualImages = new ArrayList<>();
            List<Boolean> actualMainImageFlags = new ArrayList<>();

            boolean noFilesUploaded = images == null || images.isEmpty() || (images.size() == 1 && images.get(0).isEmpty());

            if (!noFilesUploaded) {
                if (mainImageFlags == null || images.size() != mainImageFlags.size()) {
                    throw new IllegalArgumentException("Dữ liệu hình ảnh và cờ ảnh chính không đồng bộ.");
                }

                for (int i = 0; i < images.size(); i++) {
                    MultipartFile image = images.get(i);
                    if (image != null && !image.isEmpty()) {
                        actualImages.add(image);
                        actualMainImageFlags.add("true".equalsIgnoreCase(mainImageFlags.get(i)));
                    }
                }
            }

            Product createdProduct = productService.createProduct(productCreateReq, actualImages, actualMainImageFlags);
            redirectAttributes.addFlashAttribute("successMessage", "Sản phẩm '" + createdProduct.getName() + "' đã được tạo thành công!");

            return "redirect:/admin/products";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Đã có lỗi xảy ra trong quá trình tạo sản phẩm.");
            loadFormAuxiliaryData(model);
            model.addAttribute("productCreateReq", productCreateReq);

            return "admin/pages/product/create";
        }
    }

    private void loadFormAuxiliaryData(Model model) {
        try {
            List<Category> categories = categoryService.findAllActiveCategories();
            model.addAttribute("categories", categories);

            List<Origin> origins = originService.findAllActiveOrigins();
            model.addAttribute("origins", origins);
        } catch (Exception e) {
            model.addAttribute("pageErrorMessage", "Không thể tải dữ liệu cần thiết cho form.");
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditProductForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.getProductById(id);

            ProductUpdateReq productDTO = productMapper.toProductUpdateReq(product);

            model.addAttribute("pageMetadata", breadcrumbService.buidAdminProductDetail());
            model.addAttribute("productDTO", productDTO);
            model.addAttribute("existingImages", product.getImages());
            model.addAttribute("categories", categoryService.findAllActiveCategories());
            model.addAttribute("origins", originService.findAllActiveOrigins());

            return "admin/pages/product/update";

        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi không mong muốn xảy ra khi tải trang.");
            return "redirect:/admin/products";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable("id") Long id,
                                @Valid @ModelAttribute("productDTO") ProductUpdateReq productDTO,
                                BindingResult bindingResult,
                                @RequestParam(name = "newImages", required = false) List<MultipartFile> newImages,
                                @RequestParam(name = "imageIdsToDelete", required = false) List<Long> imageIdsToDelete,
                                RedirectAttributes redirectAttributes,
                                Model model) {

        productDTO.setId(id);

        if (bindingResult.hasErrors()) {
            populateFormModelOnError(model, productDTO);
            return "admin/pages/product/update";
        }

        try {
            Product updatedProduct = productService.updateProduct(id, productDTO, newImages, imageIdsToDelete);
            redirectAttributes.addFlashAttribute("successMessage", "Sản phẩm '" + updatedProduct.getName() + "' đã được cập nhật thành công!");
            return "redirect:/admin/products";

        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cập nhật sản phẩm thất bại: " + e.getMessage());
            return "redirect:/admin/products/edit/" + id;
        }
    }

    private void populateFormModelOnError(Model model, ProductUpdateReq productDTO) {
        model.addAttribute("pageMetadata", breadcrumbService.buildAdminOriginDetailPageMetadata());
        model.addAttribute("categories", categoryService.findAllActiveCategories());
        model.addAttribute("origins", originService.findAllActiveOrigins());


        if (productDTO.getId() != null) {
            try {
                Product existingProduct = productService.getProductById(productDTO.getId());
                model.addAttribute("existingImages", existingProduct.getImages());
            } catch (ResourceNotFoundException e) {

                model.addAttribute("pageErrorMessage", "Không thể tải lại ảnh cho sản phẩm không tồn tại.");
            }
        }
    }

    @GetMapping
    public String listProducts(Model model, @PageableDefault(size = 5, sort = "id", direction = Direction.DESC) Pageable pageable, @ModelAttribute("criteria") ProductSearchCriteriaReq criteria) {
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
    public String deleteProductByForm(@PathVariable("productId") Long id, RedirectAttributes redirectAttributes) {
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
