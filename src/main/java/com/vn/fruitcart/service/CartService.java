package com.vn.fruitcart.service;

import java.math.BigDecimal;
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
import com.vn.fruitcart.entity.ProductVariant;
import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.exception.ResourceNotFoundException;
import com.vn.fruitcart.repository.CartItemRepository;
import com.vn.fruitcart.repository.CartRepository;
import com.vn.fruitcart.repository.InventoryRepository;
import com.vn.fruitcart.repository.ProductVariantRepository;
import com.vn.fruitcart.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal() instanceof String
                        && "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResourceNotFoundException("Người dùng chưa đăng nhập hoặc phiên làm việc không hợp lệ.");
        }

        String username;
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetailsCustom) {
            username = ((UserDetailsCustom) principal).getCurrentUsername();
        } else if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            throw new ResourceNotFoundException(
                    "Không thể xác định thông tin người dùng từ phiên làm việc. Principal type: "
                            + principal.getClass().getName());
        }

        return userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy người dùng với email: " + username + " trong hệ thống."));
    }

    @Transactional
    public Cart getOrCreateCartForCurrentUser() {
        User currentUser = getCurrentAuthenticatedUser();
        return cartRepository.findByUser(currentUser).orElseGet(() -> {
            Cart newCart = new Cart(currentUser);
            return cartRepository.save(newCart);
        });
    }

    public Optional<Cart> findCartByCurrentUser() {
        try {
            User currentUser = getCurrentAuthenticatedUser();
            return cartRepository.findByUser(currentUser);
        } catch (ResourceNotFoundException e) {
            return Optional.empty();
        }
    }

    private Integer getStockQuantity(ProductVariant productVariant) {
        Optional<Inventory> inventoryOpt = inventoryRepository.findByProductVariant(productVariant);
        return inventoryOpt.map(Inventory::getQuantity).orElse(0);
    }

    @Transactional
    public Cart addItemToCart(Long productVariantId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Số lượng sản phẩm phải lớn hơn 0.");
        }
        Cart cart = getOrCreateCartForCurrentUser();

        ProductVariant productVariant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy biến thể sản phẩm với ID: " + productVariantId));

        if (productVariant.getProduct() == null) {
            throw new IllegalStateException("Biến thể sản phẩm không liên kết với sản phẩm cha hợp lệ.");
        }

        int currentStock = getStockQuantity(productVariant);

        Optional<CartItem> existingCartItemOpt = cartItemRepository.findByCartAndProductVariant(cart, productVariant);

        if (existingCartItemOpt.isPresent()) {
            CartItem existingCartItem = existingCartItemOpt.get();
            int newQuantityInCart = existingCartItem.getQuantity() + quantity;
            if (currentStock < newQuantityInCart) {
                throw new IllegalArgumentException("Thêm số lượng này (" + quantity
                        + ") sẽ làm tổng số lượng trong giỏ (" + newQuantityInCart + ") vượt quá tồn kho ("
                        + currentStock + ") của sản phẩm: " + productVariant.getProduct().getName()
                        + (productVariant.getAttribute() != null ? " - " + productVariant.getAttribute() : ""));
            }
            existingCartItem.setQuantity(newQuantityInCart);
            cartItemRepository.save(existingCartItem);
        } else {
            if (currentStock < quantity) {
                throw new IllegalArgumentException("Số lượng yêu cầu (" + quantity + ") vượt quá số lượng tồn kho ("
                        + currentStock + ") của sản phẩm: " + productVariant.getProduct().getName()
                        + (productVariant.getAttribute() != null ? " - " + productVariant.getAttribute() : ""));
            }
            BigDecimal price = productVariant.getPrice();
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException("Sản phẩm/Biến thể sản phẩm " + productVariant.getProduct().getName()
                        + (productVariant.getAttribute() != null ? " - " + productVariant.getAttribute() : "")
                        + " không có giá hợp lệ.");
            }

            CartItem newCartItem = new CartItem(cart, productVariant, quantity, price);
            cart.getItems().add(newCartItem);
        }
        return updateAndSaveCartTotalPrice(cart);
    }

    @Transactional
    public Cart updateItemInCart(Long cartItemId, Integer quantity) {
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Số lượng không hợp lệ.");
        }
        User currentUser = getCurrentAuthenticatedUser();
        Cart cart = cartRepository.findByUser(currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng cho người dùng."));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Không tìm thấy mục trong giỏ hàng với ID: " + cartItemId));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new SecurityException("Bạn không có quyền sửa mục này trong giỏ hàng.");
        }

        ProductVariant productVariant = cartItem.getProductVariant();
        if (productVariant == null) {
            throw new IllegalStateException("Mục trong giỏ hàng không liên kết với sản phẩm hợp lệ.");
        }

        if (quantity > 0) {
            int currentStock = getStockQuantity(productVariant);
            if (currentStock < quantity) {
                throw new IllegalArgumentException("Số lượng yêu cầu (" + quantity + ") vượt quá số lượng tồn kho ("
                        + currentStock + ") của sản phẩm: " + productVariant.getProduct().getName()
                        + (productVariant.getAttribute() != null ? " - " + productVariant.getAttribute() : ""));
            }
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        } else {
            cart.getItems().remove(cartItem);
            cartItemRepository.delete(cartItem);
        }
        return updateAndSaveCartTotalPrice(cart);
    }

    @Transactional
    public Cart removeItemFromCart(Long cartItemId) {
        User currentUser = getCurrentAuthenticatedUser();
        Cart cart = cartRepository.findByUser(currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng cho người dùng."));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Không tìm thấy mục trong giỏ hàng với ID: " + cartItemId));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new SecurityException("Bạn không có quyền xoá mục này khỏi giỏ hàng.");
        }

        cart.getItems().remove(cartItem);
        cartItemRepository.delete(cartItem);

        return updateAndSaveCartTotalPrice(cart);
    }

    @Transactional
    public Cart clearCart() {
        Cart cart = getOrCreateCartForCurrentUser();

        Cart managedCart = cartRepository.findById(cart.getId()).orElse(cart);

        if (managedCart.getItems() != null && !managedCart.getItems().isEmpty()) {
            cartItemRepository.deleteByCartId(managedCart.getId());
            managedCart.getItems().clear();
        }

        return updateAndSaveCartTotalPrice(managedCart);
    }

    private Cart updateAndSaveCartTotalPrice(Cart cart) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        Cart managedCart = cartRepository.findById(cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Giỏ hàng không tồn tại để cập nhật tổng tiền."));

        List<CartItem> items = managedCart.getItems();

        if (items != null) {
            for (CartItem item : items) {
                totalPrice = totalPrice.add(item.getSubtotal());
            }
        }
        managedCart.setTotalPrice(totalPrice);
        return cartRepository.save(managedCart);
    }

    public int getItemCountInCart() {
        try {
            Optional<Cart> cartOpt = findCartByCurrentUser();
            if (cartOpt.isPresent()) {
                Cart cart = cartOpt.get();
                Cart managedCart = cartRepository.findById(cart.getId()).orElse(cart);
                List<CartItem> items = managedCart.getItems();

                if (items == null)
                    return 0;
                return items.stream().mapToInt(CartItem::getQuantity).sum();
            }
        } catch (ResourceNotFoundException e) {
            return 0;
        }
        return 0;
    }

    public Cart saveCart(Cart cart) {
        return cartRepository.save(cart);
    }
}
