package com.vn.fruitcart.service;

import com.vn.fruitcart.entity.*;
import com.vn.fruitcart.entity.dto.SalesDataDto;
import com.vn.fruitcart.entity.dto.request.OrderCheckoutRequest;
import com.vn.fruitcart.entity.dto.request.OrderSearchCriteria;
import com.vn.fruitcart.exception.OutOfStockException;
import com.vn.fruitcart.exception.ResourceNotFoundException;
import com.vn.fruitcart.repository.OrderRepository;
import com.vn.fruitcart.util.constant.EOrderStatus;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final UserService userService;

    public Optional<Order> findById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    @Transactional
    public Order createOrderFromCart(OrderCheckoutRequest checkoutReq) {
        Cart cart = cartService.getCartForCurrentUser();

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Không thể tạo đơn hàng từ giỏ hàng rỗng.");
        }

        Order order = new Order();
        order.setUser(userService.getCurrentUser());
        order.setStatus(EOrderStatus.PENDING);

        order.setCustomerName(checkoutReq.getCustomerName());
        order.setPhoneNumber(checkoutReq.getPhoneNumber());
        order.setNote(checkoutReq.getNote());

        String fullAddress = String.join(", ",
                checkoutReq.getStreetAddress(),
                checkoutReq.getWardName(),
                checkoutReq.getDistrictName(),
                checkoutReq.getProvinceName()
        );
        order.setAddress(fullAddress);

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductVariant(cartItem.getProductVariant());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtOrder(cartItem.getUnitPrice());

            Inventory inventory = cartItem.getProductVariant().getInventory();
            if (inventory == null || inventory.getQuantity() < cartItem.getQuantity()) {
                throw new OutOfStockException("Sản phẩm '" +
                        cartItem.getProductVariant().getProduct().getName() + " - " +
                        cartItem.getProductVariant().getAttribute() + "' không đủ số lượng tồn kho.");
            }
            inventory.setQuantity(inventory.getQuantity() - cartItem.getQuantity());

            order.addOrderItem(orderItem);
        }

        order.setTotalAmount(cart.getTotal());
        Order savedOrder = orderRepository.save(order);

        cartService.clearCart();

        return savedOrder;
    }

    public Page<Order> findOrdersForCurrentUser(Pageable pageable) {
        User currentUser = userService.getCurrentUser();
        return orderRepository.findByUser(currentUser, pageable);
    }

    public Optional<Order> findOrderByIdForCurrentUser(Long orderId) {
        User currentUser = userService.getCurrentUser();
        return orderRepository.findByIdAndUser(orderId, currentUser);
    }


    public Page<Order> findByCriteria(OrderSearchCriteria criteria, Pageable pageable) {
        Specification<Order> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(criteria.getKeyword())) {
                try {
                    Long orderId = Long.parseLong(criteria.getKeyword());
                    predicates.add(criteriaBuilder.equal(root.get("id"), orderId));
                } catch (NumberFormatException e) {
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("customerName")),
                            "%" + criteria.getKeyword().toLowerCase() + "%"));
                }
            }

            if (criteria.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), criteria.getStatus()));
            }

            if (criteria.getFromDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"), criteria.getFromDate().atStartOfDay()));
            }
            if (criteria.getToDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdDate"), criteria.getToDate().atTime(23, 59, 59)));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return orderRepository.findAll(spec, pageable);
    }

    @Transactional
    public void updateStatus(Long orderId, EOrderStatus newStatus) {
        Order order = findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));

        EOrderStatus currentStatus = order.getStatus();

        if (currentStatus == newStatus) {
            return;
        }

        if (currentStatus == EOrderStatus.COMPLETED || currentStatus == EOrderStatus.CANCELLED) {
            throw new IllegalStateException("Không thể thay đổi trạng thái của đơn hàng đã hoàn thành hoặc đã bị hủy.");
        }

        if (newStatus == EOrderStatus.CANCELLED) {
            for (OrderItem item : order.getOrderItems()) {
                Inventory inventory = item.getProductVariant().getInventory();
                if (inventory != null) {
                    int newQuantity = inventory.getQuantity() + item.getQuantity();
                    inventory.setQuantity(newQuantity);
                }
            }
        }

        order.setStatus(newStatus);

        orderRepository.save(order);
    }

    public List<SalesDataDto> getMonthlySalesDataLastYear() {
        Instant startDate = LocalDateTime.now().minusMonths(11).withDayOfMonth(1)
                .toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC);

        List<Object[]> results = orderRepository.findMonthlySalesSince(startDate);

        Map<String, BigDecimal> salesByMonthMap = results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (BigDecimal) row[1]
                ));

        List<SalesDataDto> fullSalesData = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (int i = 11; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            String monthKey = month.format(formatter);
            BigDecimal revenue = salesByMonthMap.getOrDefault(monthKey, BigDecimal.ZERO);
            fullSalesData.add(new SalesDataDto(monthKey, revenue));
        }

        return fullSalesData;
    }

    public Long countTotalOrders() {
        return orderRepository.count();
    }

    public BigDecimal getTotalRevenue() {
        BigDecimal totalRevenue = orderRepository.sumTotalAmountByStatus(EOrderStatus.COMPLETED);

        return totalRevenue == null ? BigDecimal.ZERO : totalRevenue;
    }

    public List<Order> findRecentOrders() {
        return orderRepository.findTop5ByStatusOrderByCreatedDateDesc(EOrderStatus.PENDING);
    }
}