package com.vn.fruitcart.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.vn.fruitcart.entity.Cart;
import com.vn.fruitcart.entity.CartItem;
import com.vn.fruitcart.entity.Order;
import com.vn.fruitcart.entity.OrderItem;
import com.vn.fruitcart.entity.Origin;
import com.vn.fruitcart.entity.ProductVariant;
import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.entity.dto.request.OrderCheckoutRequest;
import com.vn.fruitcart.entity.dto.request.OrderSearchCriteria;
import com.vn.fruitcart.entity.dto.response.UserSessionInfo;
import com.vn.fruitcart.entity.dto.response.user.AdminUserDetailRes;
import com.vn.fruitcart.exception.ResourceNotFoundException;
import com.vn.fruitcart.repository.OrderRepository;
import com.vn.fruitcart.repository.ProductVariantRepository;
import com.vn.fruitcart.repository.UserRepository;
import com.vn.fruitcart.service.specification.OrderSpecification;
import com.vn.fruitcart.service.specification.OriginSpecification;
import com.vn.fruitcart.util.constant.OrderStatusEnum;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final CartService cartService;

    @Transactional()
    public Order createOrderFromCart(OrderCheckoutRequest checkoutRequest, HttpSession session) {
        Cart cart = cartService.getCartForSession(session);
        UserSessionInfo userSessionInfo = (UserSessionInfo) session.getAttribute("loggedInUser");

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Giỏ hàng đang trống.");
        }
        if (userSessionInfo == null) {
            throw new SecurityException("Vui lòng đăng nhập để đặt hàng.");
        }

        Order order = new Order();
        User user = userRepository.findById(userSessionInfo.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Set thông tin chung cho đơn hàng
        order.setUser(user);
        order.setStatus(OrderStatusEnum.DELIVERED);
        order.setReceiverName(checkoutRequest.getReceiverName());
        order.setShippingAddress(checkoutRequest.getShippingAddress());
        order.setPhoneNumber(checkoutRequest.getPhoneNumber());
        order.setNotes(checkoutRequest.getNotes());

        BigDecimal grandTotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cart.getItems()) {
            ProductVariant variant = productVariantRepository.findById(cartItem.getProductVariant().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại."));

            if (variant.getInventory().getQuantity() < cartItem.getQuantity()) {
                throw new IllegalStateException("Sản phẩm '" + variant.getProduct().getName() + "' không đủ hàng.");
            }

            variant.getInventory().setQuantity(variant.getInventory().getQuantity() - cartItem.getQuantity());
            productVariantRepository.save(variant);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductVariant(variant);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(variant.getPrice());
            orderItems.add(orderItem);

            grandTotal = grandTotal.add(variant.getPrice().multiply(new BigDecimal(cartItem.getQuantity())));
        }

        order.setItems(orderItems);
        order.setTotalAmount(grandTotal);

        Order savedOrder = orderRepository.save(order);
        cartService.clearCart();

        return savedOrder;
    }

    public Page<Order> findOrdersByCriteria(OrderSearchCriteria criteria, Pageable pageable) {
        Specification<Order> spec = Specification.where(null);

        if (criteria.getKeyword() != null && !criteria.getKeyword().isEmpty()) {
            spec = spec.and(OrderSpecification.hasName(criteria.getKeyword()));
        }

        if (criteria.getStatus() != null) {
            spec = spec.and(OrderSpecification.hasStatus(criteria.getStatus()));
        }

        return orderRepository.findAll(spec, pageable);
    }
}
