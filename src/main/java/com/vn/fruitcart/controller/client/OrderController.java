package com.vn.fruitcart.controller.client;

import com.vn.fruitcart.entity.Cart;
import com.vn.fruitcart.entity.Order;
import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.entity.dto.request.OrderCheckoutRequest;
import com.vn.fruitcart.entity.dto.response.cart.CartDetailRes;
import com.vn.fruitcart.exception.ResourceNotFoundException;
import com.vn.fruitcart.service.BreadcrumbService;
import com.vn.fruitcart.service.CartService;
import com.vn.fruitcart.service.OrderService;
import com.vn.fruitcart.service.UserService;
import com.vn.fruitcart.util.mapper.CartMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/thanh-toan")
@RequiredArgsConstructor
public class OrderController {

    private final CartService cartService;
    private final OrderService orderService;
    private final UserService userService;
    private final CartMapper cartMapper;
    private final BreadcrumbService breadcrumbService;


    @GetMapping()
    public String checkoutPage(Model model, RedirectAttributes redirectAttributes) {
        Cart cart = cartService.getCartForCurrentUser();

        if (cart.getItems().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng của bạn đang trống, không thể thanh toán.");
            return "redirect:/gio-hang";
        }

        OrderCheckoutRequest checkoutForm = getOrderCheckoutRequest();

        CartDetailRes cartDetail = cartMapper.toCartDetailRes(cart);
        model.addAttribute("cart", cartDetail);
        model.addAttribute("checkoutForm", checkoutForm);
        model.addAttribute("pageMetadata", breadcrumbService.buildCheckout());

        return "client/pages/order/checkout";
    }

    private OrderCheckoutRequest getOrderCheckoutRequest() {
        User currentUser = userService.getCurrentUser();
        OrderCheckoutRequest checkoutForm = new OrderCheckoutRequest();

        checkoutForm.setCustomerName(currentUser.getFullName());
        checkoutForm.setPhoneNumber(currentUser.getPhone());


        checkoutForm.setWardName(currentUser.getWard());
        checkoutForm.setDistrictName(currentUser.getDistrict());
        checkoutForm.setProvinceName(currentUser.getProvince());
        checkoutForm.setStreetAddress(currentUser.getAddressDetail());
        return checkoutForm;
    }

    @GetMapping("/success")
    public String orderSuccessPage(@RequestParam("orderId") Long orderId, Model model) {
        Order order = orderService.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng."));

        model.addAttribute("order", order);
        model.addAttribute("pageMetadata", cartService.getCartForCurrentUser());
        model.addAttribute("pageMetadata", breadcrumbService.buildCheckoutSussces());
        return "client/pages/order/success";
    }
}