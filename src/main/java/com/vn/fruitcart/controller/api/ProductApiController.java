package com.vn.fruitcart.controller.api;

import com.vn.fruitcart.entity.Product;
import com.vn.fruitcart.entity.dto.request.QuickViewProductDTO;
import com.vn.fruitcart.exception.ResourceNotFoundException;
import com.vn.fruitcart.service.ProductService;
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

    @GetMapping("/products/quick-view/{slug}")
    public ResponseEntity<QuickViewProductDTO> getProductForQuickView(@PathVariable String slug) {
        Product product = productService.getProductBySlug(slug);

        QuickViewProductDTO dto = new QuickViewProductDTO();
        dto.setName(product.getName());
        dto.setShortDescription(product.getShortDescription());
        dto.setMainImage(product.getMainImage() != null ? product.getMainImage().getImageUrl() : null);

        List<QuickViewProductDTO.VariantInfo> variantInfos = product.getVariants().stream()
                .map(v -> {
                    QuickViewProductDTO.VariantInfo info = new QuickViewProductDTO.VariantInfo();
                    info.setId(v.getId());
                    info.setAttribute(v.getAttribute());
                    info.setPrice(v.getPrice());
                    info.setSalePrice(v.getSalePrice());
                    info.setOnSale(v.isOnSale());

                    info.setQuantity(v.getInventory() != null ? v.getInventory().getQuantity() : 0);

                    return info;
                })
                .collect(Collectors.toList());
        dto.setVariants(variantInfos);

        return ResponseEntity.ok(dto);
    }
}
