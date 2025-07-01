// File: src/main/java/com/vn/fruitcart/entity/dto/response/CartItemDetailDTO.java
package com.vn.fruitcart.entity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Lớp DTO (Data Transfer Object) này dùng để chứa thông tin chi tiết của một mục hàng
 * và gửi về cho giao diện người dùng (frontend) một cách an toàn và có cấu trúc.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDetailDTO {

    // ID của chính mục trong giỏ hàng (CartItem)
    private Long cartItemId;

    // --- Thông tin về sản phẩm gốc ---
    private Long productId;
    private String productName;
    private String productSlug; // Thêm slug để tạo link sản phẩm
    private String productImageUrl;

    // --- Thông tin về biến thể sản phẩm cụ thể ---
    private Long variantId;
    private String variantAttribute;
    private int stock; // Tồn kho hiện tại của biến thể này

    // --- Thông tin về giá và số lượng trong giỏ hàng ---
    private BigDecimal unitPrice; // Đơn giá của sản phẩm tại thời điểm thêm vào giỏ
    private int quantity; // Số lượng người dùng đã chọn
    private BigDecimal subtotal; // Tổng tiền cho mục này (đơn giá * số lượng)
}