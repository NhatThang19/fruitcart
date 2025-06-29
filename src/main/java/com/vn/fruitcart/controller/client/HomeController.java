package com.vn.fruitcart.controller.client;

import java.util.Collections;
import java.util.stream.Collectors;

import com.vn.fruitcart.entity.dto.request.product.ProductSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import com.vn.fruitcart.entity.Product;
import com.vn.fruitcart.service.BreadcrumbService;
import com.vn.fruitcart.service.CategoryService;
import com.vn.fruitcart.service.OriginService;
import com.vn.fruitcart.service.ProductService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final OriginService originService;
    private final BreadcrumbService breadcrumbService;

    @GetMapping("/")
    public String getHomePage(Model model) {
        boolean hideBreadcrumb = true;
        model.addAttribute("hideBreadcrumb", hideBreadcrumb);
        return "client/pages/index";
    }

    @GetMapping("/san-pham")
    public String index(
            Model model,
            @ModelAttribute("criteria") ProductSearchCriteria criteria,
            @PageableDefault(size = 8, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {

        // ====================================================================
        //         *** BẮT ĐẦU PHẦN SỬA ĐỔI LOGIC SẮP XẾP ***
        // ====================================================================

        // 1. Tạo tiêu chí sắp xếp chính: luôn ưu tiên 'isNew' giảm dần (true lên trước)
        Sort newProductSort = Sort.by(Sort.Direction.DESC, "isNew");

        // 2. Lấy tiêu chí sắp xếp phụ từ yêu cầu của người dùng (ví dụ: theo giá, theo tên)
        Sort userSort = pageable.getSort();

        // 3. Kết hợp hai tiêu chí: ưu tiên isNew, sau đó mới đến tiêu chí của người dùng
        Sort finalSort = newProductSort.and(userSort);

        // 4. Tạo một đối tượng Pageable mới với thông tin phân trang cũ và tiêu chí sắp xếp mới
        Pageable finalPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), finalSort);

        // 5. Sử dụng Pageable mới này để gọi service
        Page<Product> productPage = productService.searchProducts(criteria, finalPageable);

        // ====================================================================
        //          *** KẾT THÚC PHẦN SỬA ĐỔI LOGIC SẮP XẾP ***
        // ====================================================================


        String currentSort = pageable.getSort().get()
                .map(order -> order.getProperty() + "," + order.getDirection().name().toLowerCase())
                .collect(Collectors.joining());

        model.addAttribute("currentSort", currentSort);

        model.addAttribute("productPage", productPage);
        model.addAttribute("categories", categoryService.findAllActiveCategories());
        model.addAttribute("origins", originService.findAllActiveOrigins());
        model.addAttribute("pageMetadata", breadcrumbService.demo());

        model.addAttribute("productPage", productPage);
        model.addAttribute("categories", categoryService.findAllActiveCategories());
        model.addAttribute("origins", originService.findAllActiveOrigins());

        model.addAttribute("pageMetadata", breadcrumbService.demo());

        return "client/pages/product/list";
    }

    @GetMapping("/about-us")
    public String getAboutUsPage(Model model) {
        model.addAttribute("pageMetadata", breadcrumbService.demo());
        return "client/pages/about-us";
    }

    @GetMapping("/contact")
    public String getContactPage(Model model) {
        model.addAttribute("pageMetadata", breadcrumbService.demo());
        return "client/pages/contact";
    }

}
