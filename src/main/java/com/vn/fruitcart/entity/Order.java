// File: src/main/java/com/vn/fruitcart/entity/Order.java
package com.vn.fruitcart.entity;

import com.vn.fruitcart.entity.base.BaseEntity;
import com.vn.fruitcart.util.constant.EOrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders") // Đặt tên bảng là "orders" để tránh trùng từ khóa SQL
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String address;

    @Column
    private String note;

    // --- Thông tin về đơn hàng ---
    @Column(name = "total_amount", nullable = false, precision = 12)
    private BigDecimal totalAmount; // Tổng tiền cuối cùng của đơn hàng

    @Enumerated(EnumType.STRING) // Lưu trạng thái dưới dạng chuỗi (eg: PENDING, PROCESSING)
    @Column(name = "status", nullable = false)
    private EOrderStatus status;

    // --- Mối quan hệ ---
    /**
     * Mỗi đơn hàng thuộc về một người dùng.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Một đơn hàng có nhiều chi tiết đơn hàng (OrderItem).
     * cascade = CascadeType.ALL: Khi lưu Order, các OrderItem liên quan cũng sẽ được lưu.
     */
    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<OrderItem> orderItems = new ArrayList<>();


    // --- Phương thức tiện ích ---

    /**
     * Thêm một OrderItem vào đơn hàng và thiết lập mối quan hệ hai chiều.
     * @param item Chi tiết đơn hàng cần thêm.
     */
    public void addOrderItem(OrderItem item) {
        this.orderItems.add(item);
        item.setOrder(this);
    }
}