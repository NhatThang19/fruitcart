package com.vn.fruitcart.controller.client;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vn.fruitcart.entity.Order;
import com.vn.fruitcart.entity.dto.request.OrderCheckoutRequest;
import com.vn.fruitcart.entity.dto.response.CartItemDetailDTO;
import com.vn.fruitcart.exception.ResourceNotFoundException;
import com.vn.fruitcart.repository.OrderRepository;
import com.vn.fruitcart.service.BreadcrumbService;
import com.vn.fruitcart.service.CartService;
import com.vn.fruitcart.service.OrderService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    private final CartService cartService;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final BreadcrumbService breadcrumbService;

    // 1. HIỂN THỊ TRANG THANH TOÁN (CHECKOUT)
    @GetMapping("/checkout")
    public String showCheckoutPage(Model model, HttpSession session) {
        List<CartItemDetailDTO> cartItems = cartService.getCartItemDetails(session);

        // Nếu giỏ hàng trống, chuyển hướng người dùng về trang giỏ hàng
        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        // Tính tổng tiền để hiển thị tóm tắt
        BigDecimal grandTotal = cartItems.stream()
                .map(CartItemDetailDTO::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("grandTotal", grandTotal);
        // Tạo một đối tượng rỗng để Thymeleaf binding dữ liệu form
        model.addAttribute("checkoutRequest", new OrderCheckoutRequest());

        model.addAttribute("pageMetadata", breadcrumbService.buildAdminOriginDetailPageMetadata());

        return "client/pages/order/checkout"; // Trỏ tới file view mà chúng ta sẽ tạo ở bước sau
    }

    // 2. XỬ LÝ YÊU CẦU ĐẶT HÀNG
    @PostMapping("/place")
    public String placeOrder(@Valid @ModelAttribute("checkoutRequest") OrderCheckoutRequest checkoutRequest,
            BindingResult bindingResult,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Nếu dữ liệu form không hợp lệ (ví dụ: bỏ trống trường bắt buộc)
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng điền đầy đủ thông tin được yêu cầu.");
            // Trả lại dữ liệu đã nhập để người dùng không phải gõ lại
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.checkoutRequest",
                    bindingResult);
            redirectAttributes.addFlashAttribute("checkoutRequest", checkoutRequest);
            return "redirect:/order/checkout";
        }

        try {
            // Gọi service để tạo đơn hàng
            Order newOrder = orderService.createOrderFromCart(checkoutRequest, session);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đặt hàng thành công! Mã đơn hàng của bạn là #" + newOrder.getId());
            return "redirect:/order/success/" + newOrder.getId();
        } catch (IllegalStateException | SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cart";
        } catch (Exception e) {
            log.error("Lỗi nghiêm trọng khi đặt hàng: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Đã có lỗi hệ thống xảy ra. Vui lòng thử lại sau.");
            return "redirect:/order/checkout";
        }
    }

    // 3. HIỂN THỊ TRANG ĐẶT HÀNG THÀNH CÔNG
    @GetMapping("/success/{orderId}")
    public String showOrderSuccessPage(@PathVariable Long orderId, Model model) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));

        model.addAttribute("order", order);
        model.addAttribute("pageMetadata", breadcrumbService.buildAdminOriginDetailPageMetadata());
        return "client/pages/order/success"; 
    }
}
