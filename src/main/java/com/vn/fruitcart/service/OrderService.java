package com.vn.fruitcart.service;

import com.vn.fruitcart.entity.*;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

        // 1. Điền thông tin khách hàng từ request
        order.setCustomerName(checkoutReq.getCustomerName());
        order.setPhoneNumber(checkoutReq.getPhoneNumber());
        order.setNote(checkoutReq.getNote());

        // 2. Ghép các thành phần địa chỉ lại thành một chuỗi duy nhất
        String fullAddress = String.join(", ",
                checkoutReq.getStreetAddress(),
                checkoutReq.getWardName(),
                checkoutReq.getDistrictName(),
                checkoutReq.getProvinceName()
        );
        order.setAddress(fullAddress);

        // 3. Chuyển CartItem thành OrderItem và cập nhật tồn kho
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductVariant(cartItem.getProductVariant());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtOrder(cartItem.getUnitPrice()); // Lấy giá đã lưu trong giỏ

            Inventory inventory = cartItem.getProductVariant().getInventory();
            if (inventory == null || inventory.getQuantity() < cartItem.getQuantity()) {
                throw new OutOfStockException("Sản phẩm '" +
                        cartItem.getProductVariant().getProduct().getName() + " - " +
                        cartItem.getProductVariant().getAttribute() + "' không đủ số lượng tồn kho.");
            }
            inventory.setQuantity(inventory.getQuantity() - cartItem.getQuantity());

            order.addOrderItem(orderItem);
        }

        // 4. Gán tổng tiền và lưu đơn hàng
        order.setTotalAmount(cart.getTotal());
        Order savedOrder = orderRepository.save(order);

        // 5. Dọn dẹp giỏ hàng
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

    /**
     * Tìm kiếm và phân trang tất cả đơn hàng dựa trên các tiêu chí lọc.
     * @param criteria Đối tượng chứa các tiêu chí từ admin.
     * @param pageable Thông tin phân trang và sắp xếp.
     * @return Một trang (Page) các đơn hàng phù hợp.
     */
    public Page<Order> findByCriteria(OrderSearchCriteria criteria, Pageable pageable) {
        Specification<Order> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Lọc theo từ khóa (có thể là ID hoặc tên khách hàng)
            if (StringUtils.hasText(criteria.getKeyword())) {
                try {
                    // Nếu từ khóa là số, tìm theo ID đơn hàng
                    Long orderId = Long.parseLong(criteria.getKeyword());
                    predicates.add(criteriaBuilder.equal(root.get("id"), orderId));
                } catch (NumberFormatException e) {
                    // Nếu không phải số, tìm theo tên người nhận
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("customerName")),
                            "%" + criteria.getKeyword().toLowerCase() + "%"));
                }
            }

            // Lọc theo trạng thái
            if (criteria.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), criteria.getStatus()));
            }

            // Lọc theo ngày đặt hàng
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
    public Order updateStatus(Long orderId, EOrderStatus newStatus) {
        // 1. Tìm đơn hàng, nếu không thấy sẽ ném lỗi
        Order order = findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));

        // Lấy trạng thái hiện tại để kiểm tra
        EOrderStatus currentStatus = order.getStatus();

        // 2. Kiểm tra các quy tắc nghiệp vụ
        if (currentStatus == newStatus) {
            // Không làm gì nếu trạng thái không thay đổi
            return order;
        }

        if (currentStatus == EOrderStatus.COMPLETED || currentStatus == EOrderStatus.CANCELLED) {
            // Không cho phép thay đổi trạng thái của đơn hàng đã Hoàn thành hoặc đã Hủy
            throw new IllegalStateException("Không thể thay đổi trạng thái của đơn hàng đã hoàn thành hoặc đã bị hủy.");
        }

        // =============================================
        //      *** BẮT ĐẦU LOGIC HỦY ĐƠN HÀNG ***
        // =============================================
        // 3. Nếu trạng thái mới là CANCELLED, hoàn trả số lượng vào kho
        if (newStatus == EOrderStatus.CANCELLED) {
            for (OrderItem item : order.getOrderItems()) {
                Inventory inventory = item.getProductVariant().getInventory();
                if (inventory != null) {
                    // Cộng lại số lượng sản phẩm đã bị trừ khi đặt hàng
                    int newQuantity = inventory.getQuantity() + item.getQuantity();
                    inventory.setQuantity(newQuantity);
                }
            }
        }
        // =============================================
        //       *** KẾT THÚC LOGIC HỦY ĐƠN HÀNG ***
        // =============================================

        // 4. Cập nhật trạng thái mới cho đơn hàng
        order.setStatus(newStatus);

        // 5. Lưu lại đơn hàng (và các thay đổi về inventory nhờ có @Transactional)
        return orderRepository.save(order);
    }
}