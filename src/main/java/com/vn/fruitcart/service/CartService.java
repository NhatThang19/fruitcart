package com.vn.fruitcart.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.vn.fruitcart.config.UserDetailsCustom;
import com.vn.fruitcart.entity.Cart;
import com.vn.fruitcart.entity.CartItem;
import com.vn.fruitcart.entity.Inventory;
import com.vn.fruitcart.entity.Product;
import com.vn.fruitcart.entity.ProductImage;
import com.vn.fruitcart.entity.ProductVariant;
import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.entity.dto.response.CartItemDetailDTO;
import com.vn.fruitcart.entity.dto.response.UserSessionInfo;
import com.vn.fruitcart.entity.dto.response.cart.SidebarCartItemReq;
import com.vn.fruitcart.entity.dto.response.cart.SidebarCartReq;
import com.vn.fruitcart.exception.ResourceNotFoundException;
import com.vn.fruitcart.repository.CartItemRepository;
import com.vn.fruitcart.repository.CartRepository;
import com.vn.fruitcart.repository.InventoryRepository;
import com.vn.fruitcart.repository.ProductVariantRepository;
import com.vn.fruitcart.repository.UserRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final ProductVariantRepository variantRepository;
    private final UserService userService;
    private final CartItemRepository cartItemRepository;

    /**
     * Lấy giỏ hàng của người dùng đang đăng nhập.
     * Nếu người dùng chưa có giỏ hàng, một giỏ hàng mới sẽ được tạo tự động.
     *
     * @return Giỏ hàng của người dùng.
     */
    @Transactional
    public Cart getCartForCurrentUser() {
        User currentUser = userService.getCurrentUser(); // Bạn cần tự triển khai logic này
        return cartRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(currentUser);
                    return cartRepository.save(newCart);
                });
    }

    /**
     * Thêm một sản phẩm vào giỏ hàng hoặc cập nhật số lượng nếu đã tồn tại.
     *
     * @param variantId ID của biến thể sản phẩm.
     * @param quantity  Số lượng muốn thêm.
     * @return Giỏ hàng đã được cập nhật.
     */
    @Transactional
    public Cart addItemToCart(Long variantId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng sản phẩm phải lớn hơn 0.");
        }

        Cart cart = getCartForCurrentUser();
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy biến thể sản phẩm với ID: " + variantId));

        // Kiểm tra xem sản phẩm đã có trong giỏ chưa
        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProductVariant().getId().equals(variantId))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            // Nếu có, cộng thêm số lượng
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            // Nếu chưa, tạo một CartItem mới
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

    /**
     * Cập nhật số lượng cho một mục trong giỏ hàng.
     *
     * @param cartItemId ID của biến thể sản phẩm.
     * @param quantity  Số lượng mới (phải > 0).
     * @return Giỏ hàng đã được cập nhật.
     */
    @Transactional
    public Cart updateItemQuantity(Long cartItemId, int quantity) {
        if (quantity <= 0) {
            // Nếu số lượng mới là 0 hoặc âm, ta coi như là xóa sản phẩm
            return removeItemFromCart(cartItemId);
        }

        Cart cart = getCartForCurrentUser();
        CartItem itemToUpdate = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mục trong giỏ hàng với ID: " + cartItemId));

        // Kiểm tra xem mục này có thực sự thuộc về giỏ hàng của người dùng không
        if (!itemToUpdate.getCart().getId().equals(cart.getId())) {
            throw new SecurityException("Bạn không có quyền thay đổi mục này.");
        }

        itemToUpdate.setQuantity(quantity);
        cartItemRepository.save(itemToUpdate);
        return cart;
    }

    /**
     * Xóa một mục khỏi giỏ hàng.
     *
     * @param cartItemId ID của biến thể sản phẩm cần xóa.
     * @return Giỏ hàng đã được cập nhật.
     */
    @Transactional
    public Cart removeItemFromCart(Long cartItemId) {
        Cart cart = getCartForCurrentUser();
        CartItem itemToRemove = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mục trong giỏ hàng với ID: " + cartItemId));

        if (!itemToRemove.getCart().getId().equals(cart.getId())) {
            throw new SecurityException("Bạn không có quyền xóa mục này.");
        }

        // Xóa mục khỏi danh sách và orphanRemoval=true sẽ xóa nó khỏi DB
        cart.getItems().remove(itemToRemove);
        return cartRepository.save(cart);
    }

    /**
     * Xóa toàn bộ sản phẩm khỏi giỏ hàng.
     *
     * @return Giỏ hàng trống.
     */
    @Transactional
    public Cart clearCart() {
        Cart cart = getCartForCurrentUser();
        cart.getItems().clear(); // Nhờ orphanRemoval=true, các CartItem sẽ bị xóa
        return cartRepository.save(cart);
    }
}
