// File: src/main/java/com/vn/fruitcart/util/mapper/CartMapper.java
package com.vn.fruitcart.util.mapper;

import com.vn.fruitcart.entity.Cart;
import com.vn.fruitcart.entity.CartItem;
import com.vn.fruitcart.entity.Product;
import com.vn.fruitcart.entity.ProductVariant;
import com.vn.fruitcart.entity.dto.response.CartItemDetailDTO;
import com.vn.fruitcart.entity.dto.response.cart.CartDetailRes;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartMapper {

    /**
     * Chuyển đổi từ thực thể Cart sang DTO CartDetailRes.
     * DTO này chứa toàn bộ thông tin cần thiết để hiển thị giỏ hàng.
     *
     * @param cart Thực thể Cart lấy từ cơ sở dữ liệu.
     * @return Đối tượng DTO để trả về cho client.
     */
    public CartDetailRes toCartDetailRes(Cart cart) {
        // Nếu giỏ hàng là null, trả về một giỏ hàng rỗng để tránh lỗi
        if (cart == null) {
            return CartDetailRes.builder()
                    .items(Collections.emptyList())
                    .totalPrice(java.math.BigDecimal.ZERO)
                    .totalItems(0)
                    .build();
        }

        List<CartItemDetailDTO> itemDTOs = cart.getItems().stream()
                .map(this::toCartItemDetailDTO) // Sử dụng phương thức helper bên dưới
                .collect(Collectors.toList());

        return CartDetailRes.builder()
                .items(itemDTOs)
                .totalPrice(cart.getTotal())
                .totalItems(cart.getTotalItems())
                .build();
    }

    /**
     * Chuyển đổi từ một thực thể CartItem sang DTO CartItemDetailDTO.
     *
     * @param cartItem Thực thể CartItem.
     * @return Đối tượng DTO chi tiết của một mục trong giỏ hàng.
     */
    public CartItemDetailDTO toCartItemDetailDTO(CartItem cartItem) {
        if (cartItem == null) {
            return null;
        }

        CartItemDetailDTO dto = new CartItemDetailDTO();

        // 1. Gán các thông tin trực tiếp từ CartItem
        dto.setCartItemId(cartItem.getId()); // SỬA LỖI: Tên trường đúng là cartItemId
        dto.setQuantity(cartItem.getQuantity());
        dto.setUnitPrice(cartItem.getUnitPrice());
        dto.setSubtotal(cartItem.getSubtotal());

        // 2. Lấy thông tin từ ProductVariant một cách an toàn
        ProductVariant variant = cartItem.getProductVariant();
        if (variant != null) {
            dto.setVariantId(variant.getId());
            dto.setVariantAttribute(variant.getAttribute());

            // BỔ SUNG: Lấy số lượng tồn kho
            dto.setStock(variant.getInventory() != null ? variant.getInventory().getQuantity() : 0);

            // 3. Lấy thông tin từ Product gốc một cách an toàn
            Product product = variant.getProduct();
            if (product != null) {
                dto.setProductId(product.getId());
                dto.setProductName(product.getName());
                dto.setProductSlug(product.getSlug());
                dto.setProductImageUrl(product.getMainImage() != null ? product.getMainImage().getImageUrl() : null);
            }
        }
        return dto;
    }
}