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
@RequiredArgsConstructor
public class CartApiController {

    private final CartService cartService;
    private final CartMapper cartMapper;

    @GetMapping
    public ResponseEntity<CartDetailRes> getCartDetails() {
        Cart cart = cartService.getCartForCurrentUser();
        return ResponseEntity.ok(cartMapper.toCartDetailRes(cart));
    }

    @PostMapping("/items")
    public ResponseEntity<CartDetailRes> addItem(@Valid @RequestBody AddToCartRequest request) {
        Cart updatedCart = cartService.addItemToCart(request.getProductVariantId(), request.getQuantity());
        return ResponseEntity.ok(cartMapper.toCartDetailRes(updatedCart));
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartDetailRes> updateItem(@PathVariable Long cartItemId,
                                                    @Valid @RequestBody UpdateCartItemRequest request) {
        Cart updatedCart = cartService.updateItemQuantity(cartItemId, request.getQuantity());
        return ResponseEntity.ok(cartMapper.toCartDetailRes(updatedCart));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartDetailRes> removeItem(@PathVariable Long cartItemId) {
        Cart updatedCart = cartService.removeItemFromCart(cartItemId);
        return ResponseEntity.ok(cartMapper.toCartDetailRes(updatedCart));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Integer>> getCartItemCount() {
        Cart cart = cartService.getCartForCurrentUser();
        return ResponseEntity.ok(Map.of("count", cart.getTotalItems()));
    }
}