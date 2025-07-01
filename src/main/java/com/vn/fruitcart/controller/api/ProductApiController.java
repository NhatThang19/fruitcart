package com.vn.fruitcart.controller.api;

import com.vn.fruitcart.entity.Product;
import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.entity.dto.request.QuickViewProductDTO;
import com.vn.fruitcart.exception.ResourceNotFoundException;
import com.vn.fruitcart.service.ProductService;
import com.vn.fruitcart.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductApiController {
    private final ProductService productService;
    private final UserService userService;

    @GetMapping("/products/quick-view/{slug}")
    public ResponseEntity<QuickViewProductDTO> getProductForQuickView(@PathVariable String slug) {

        // --- BẮT ĐẦU PHẦN SỬA ĐỔI ---

        // 1. Lấy người dùng hiện tại (có thể là null nếu là khách)
        User currentUser = null;
        try {
            currentUser = userService.getCurrentUser();
        } catch (Exception e) {
            // Bỏ qua lỗi nếu người dùng chưa đăng nhập, currentUser sẽ vẫn là null
        }

        // Lấy sản phẩm theo slug
        Product product = productService.getProductBySlug(slug);

        QuickViewProductDTO dto = new QuickViewProductDTO();
        dto.setName(product.getName());
        dto.setShortDescription(product.getShortDescription());
        dto.setMainImage(product.getMainImage() != null ? product.getMainImage().getImageUrl() : null);

        // Tạo biến final để sử dụng trong lambda
        final User finalCurrentUser = currentUser;

        List<QuickViewProductDTO.VariantInfo> variantInfos = product.getVariants().stream()
                .map(v -> {
                    QuickViewProductDTO.VariantInfo info = new QuickViewProductDTO.VariantInfo();
                    info.setId(v.getId());
                    info.setAttribute(v.getAttribute());
                    info.setPrice(v.getPrice());

                    // 2. Gọi các phương thức mới và truyền vào người dùng hiện tại
                    info.setSalePrice(v.getSalePriceForUser(finalCurrentUser));
                    info.setOnSale(v.isOnSaleForUser(finalCurrentUser));

                    info.setQuantity(v.getInventory() != null ? v.getInventory().getQuantity() : 0);

                    return info;
                })
                .collect(Collectors.toList());
        dto.setVariants(variantInfos);

        return ResponseEntity.ok(dto);

        // --- KẾT THÚC PHẦN SỬA ĐỔI ---
    }
}
