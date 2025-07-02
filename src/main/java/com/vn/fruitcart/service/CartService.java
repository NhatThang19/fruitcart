package com.vn.fruitcart.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.vn.fruitcart.entity.Cart;
import com.vn.fruitcart.entity.CartItem;
import com.vn.fruitcart.entity.ProductVariant;
import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.exception.ResourceNotFoundException;
import com.vn.fruitcart.repository.CartItemRepository;
import com.vn.fruitcart.repository.CartRepository;
import com.vn.fruitcart.repository.ProductVariantRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final ProductVariantRepository variantRepository;
    private final UserService userService;
    private final CartItemRepository cartItemRepository;


    @Transactional
    public Cart getCartForCurrentUser() {
        User currentUser = userService.getCurrentUser();
        return cartRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(currentUser);
                    return cartRepository.save(newCart);
                });
    }

    @Transactional
    public Cart addItemToCart(Long variantId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng sản phẩm phải lớn hơn 0.");
        }

        Cart cart = getCartForCurrentUser();
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy biến thể sản phẩm với ID: " + variantId));

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProductVariant().getId().equals(variantId))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProductVariant(variant);
            newItem.setQuantity(quantity);
            User user = userService.getCurrentUser();
            newItem.setUnitPrice(variant.getSalePriceForUser(user));
            cart.getItems().add(newItem);
        }

        return cartRepository.save(cart);
    }

    @Transactional
    public Cart updateItemQuantity(Long cartItemId, int quantity) {
        if (quantity <= 0) {
            return removeItemFromCart(cartItemId);
        }

        Cart cart = getCartForCurrentUser();
        CartItem itemToUpdate = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mục trong giỏ hàng với ID: " + cartItemId));

        if (!itemToUpdate.getCart().getId().equals(cart.getId())) {
            throw new SecurityException("Bạn không có quyền thay đổi mục này.");
        }

        itemToUpdate.setQuantity(quantity);
        cartItemRepository.save(itemToUpdate);
        return cart;
    }

    @Transactional
    public Cart removeItemFromCart(Long cartItemId) {
        Cart cart = getCartForCurrentUser();
        CartItem itemToRemove = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mục trong giỏ hàng với ID: " + cartItemId));

        if (!itemToRemove.getCart().getId().equals(cart.getId())) {
            throw new SecurityException("Bạn không có quyền xóa mục này.");
        }

        cart.getItems().remove(itemToRemove);
        return cartRepository.save(cart);
    }

    @Transactional
    public void clearCart() {
        Cart cart = getCartForCurrentUser();
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}
