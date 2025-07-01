// File: src/main/java/com/vn/fruitcart/entity/OrderItem.java
package com.vn.fruitcart.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer quantity;

    /**
     * Giá của sản phẩm tại thời điểm đặt hàng.
     * Rất quan trọng để lưu lại giá này, vì giá sản phẩm có thể thay đổi trong tương lai.
     */
    @Column(name = "price_at_order", nullable = false, precision = 12)
    private BigDecimal priceAtOrder;

    // --- Mối quan hệ ---
    /**
     * Mỗi OrderItem thuộc về một đơn hàng (Order).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * Mỗi OrderItem tương ứng với một biến thể sản phẩm (ProductVariant).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;


    // --- Phương thức tiện ích ---

    /**
     * Tính thành tiền cho mục này (giá tại thời điểm đặt hàng * số lượng).
     * @return Thành tiền của mục (BigDecimal).
     */
    public BigDecimal getSubtotal() {
        if (this.priceAtOrder == null || this.quantity == null) {
            return BigDecimal.ZERO;
        }
        return this.priceAtOrder.multiply(new BigDecimal(this.quantity));
    }
}