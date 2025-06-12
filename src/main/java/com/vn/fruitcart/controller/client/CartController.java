package com.vn.fruitcart.controller.client;

import com.vn.fruitcart.entity.Cart;
import com.vn.fruitcart.entity.CartItem;
import com.vn.fruitcart.entity.ProductImage;
import com.vn.fruitcart.entity.ProductVariant;
import com.vn.fruitcart.entity.dto.request.AddToCartRequest;
import com.vn.fruitcart.entity.dto.request.UpdateCartItemRequest;
import com.vn.fruitcart.entity.dto.response.CartItemDetailDTO;
import com.vn.fruitcart.entity.dto.response.ProductDTO;
import com.vn.fruitcart.entity.dto.response.ProductVariantDTO;
import com.vn.fruitcart.entity.dto.response.cart.SidebarCartReq;
import com.vn.fruitcart.exception.ResourceNotFoundException;
import com.vn.fruitcart.service.BreadcrumbService;
import com.vn.fruitcart.service.CartService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cart")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class CartController {

    private static final Logger log = LoggerFactory.getLogger(CartController.class);

    private final CartService cartService;
    private final BreadcrumbService breadcrumbService;

    /**
     * Hiển thị trang chi tiết giỏ hàng.
     */
    // @GetMapping
    // public String viewCart(Model model) {
    //     try {
    //         Cart cart = cartService.getOrCreateCartForCurrentUser();
    //         model.addAttribute("cart", cart);

    //         List<CartItemDetailDTO> cartItemsDetail = (cart.getItems() != null)
    //                 ? cart.getItems().stream()
    //                         .filter(item -> item.getProductVariant() != null
    //                                 && item.getProductVariant().getProduct() != null)
    //                         .map(this::mapCartItemToDetailDTO)
    //                         .collect(Collectors.toList())
    //                 : Collections.emptyList();

    //         model.addAttribute("cartItemsDetail", cartItemsDetail);

    //         model.addAttribute("pageMetadata", breadcrumbService.buildAdminOriginDetailPageMetadata());

    //     } catch (ResourceNotFoundException e) {
    //         return "redirect:/client/auth/login?message=cart_login_required";
    //     } catch (Exception e) {
    //         log.error("Lỗi khi tải trang giỏ hàng:", e);
    //         model.addAttribute("errorMessage", "Đã xảy ra lỗi khi tải giỏ hàng của bạn.");
    //         return "error/500";
    //     }
    //     return "client/pages/cart/detail";
    // }

    /**
     * API: Thêm sản phẩm vào giỏ hàng.
     */
    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addItemToCart(@RequestBody AddToCartRequest request) {
        try {
            if (request.getProductVariantId() == null || request.getQuantity() == null || request.getQuantity() <= 0) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Dữ liệu không hợp lệ."));
            }
            Cart updatedCart = cartService.addItemToCart(request.getProductVariantId(), request.getQuantity());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Sản phẩm đã được thêm vào giỏ!");
            response.put("itemCount", cartService.getItemCountInCart());
            response.put("totalPrice", updatedCart.getTotalPrice());
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("API Lỗi khi thêm vào giỏ:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi máy chủ khi thêm sản phẩm."));
        }
    }

    /**
     * API: Cập nhật số lượng của một mục trong giỏ hàng.
     */
    @PostMapping("/update/{cartItemId}")
    @ResponseBody
    public ResponseEntity<?> updateCartItem(@PathVariable Long cartItemId, @RequestBody UpdateCartItemRequest request) {
        try {
            if (request.getQuantity() == null || request.getQuantity() < 0) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Số lượng không hợp lệ."));
            }
            Cart updatedCart = cartService.updateItemInCart(cartItemId, request.getQuantity());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message",
                    request.getQuantity() == 0 ? "Sản phẩm đã được xoá khỏi giỏ!" : "Giỏ hàng đã được cập nhật!");
            response.put("itemCount", cartService.getItemCountInCart());
            response.put("totalPrice", updatedCart.getTotalPrice());
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("API Lỗi khi cập nhật giỏ hàng:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi máy chủ khi cập nhật giỏ hàng."));
        }
    }

    /**
     * API: Xóa một mục khỏi giỏ hàng.
     */
    @PostMapping("/remove/{cartItemId}")
    @ResponseBody
    public ResponseEntity<?> removeCartItem(@PathVariable Long cartItemId) {
        try {
            Cart updatedCart = cartService.removeItemFromCart(cartItemId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Sản phẩm đã được xoá khỏi giỏ!");
            response.put("itemCount", cartService.getItemCountInCart());
            response.put("totalPrice", updatedCart.getTotalPrice());
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("API Lỗi khi xoá sản phẩm khỏi giỏ:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi máy chủ khi xoá sản phẩm."));
        }
    }

    @GetMapping("/summary")
    @ResponseBody
    public ResponseEntity<SidebarCartReq> getCartSummary(HttpSession session) {
        SidebarCartReq cartSummary = cartService.getSidebarCart(session);
        return ResponseEntity.ok(cartSummary);
    }
}