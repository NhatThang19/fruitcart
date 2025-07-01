// File: src/main/java/com/vn/fruitcart/controller/api/CartApiController.java
package com.vn.fruitcart.controller.api;

import com.vn.fruitcart.entity.Cart;
import com.vn.fruitcart.entity.dto.request.AddToCartRequest;
import com.vn.fruitcart.entity.dto.request.UpdateCartItemRequest;
import com.vn.fruitcart.entity.dto.response.cart.CartDetailRes;
import com.vn.fruitcart.service.CartService;
import com.vn.fruitcart.util.mapper.CartMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor // Chú thích của Lombok để tự tạo constructor
public class CartApiController {

    private final CartService cartService;
    private final CartMapper cartMapper;

    /**
     * API để lấy thông tin chi tiết giỏ hàng hiện tại.
     */
    @GetMapping
    public ResponseEntity<CartDetailRes> getCartDetails() {
        Cart cart = cartService.getCartForCurrentUser();
        return ResponseEntity.ok(cartMapper.toCartDetailRes(cart));
    }

    /**
     * API để thêm một sản phẩm vào giỏ hàng.
     */
    @PostMapping("/items")
    public ResponseEntity<CartDetailRes> addItem(@Valid @RequestBody AddToCartRequest request) {
        Cart updatedCart = cartService.addItemToCart(request.getProductVariantId(), request.getQuantity());
        return ResponseEntity.ok(cartMapper.toCartDetailRes(updatedCart));
    }

    /**
     * API để cập nhật số lượng của một sản phẩm trong giỏ.
     *
     * @param cartItemId ID của CartItem, không phải variantId
     */
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartDetailRes> updateItem(@PathVariable Long cartItemId,
                                                    @Valid @RequestBody UpdateCartItemRequest request) {
        Cart updatedCart = cartService.updateItemQuantity(cartItemId, request.getQuantity());
        return ResponseEntity.ok(cartMapper.toCartDetailRes(updatedCart));
    }

    /**
     * API để xóa một sản phẩm khỏi giỏ hàng.
     *
     * @param cartItemId ID của CartItem
     */
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartDetailRes> removeItem(@PathVariable Long cartItemId) {
        Cart updatedCart = cartService.removeItemFromCart(cartItemId);
        return ResponseEntity.ok(cartMapper.toCartDetailRes(updatedCart));
    }

    /**
     * API để lấy tổng số lượng sản phẩm trong giỏ (hiển thị trên icon).
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Integer>> getCartItemCount() {
        Cart cart = cartService.getCartForCurrentUser();
        return ResponseEntity.ok(Map.of("count", cart.getTotalItems()));
    }
}