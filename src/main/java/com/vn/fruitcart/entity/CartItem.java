package com.vn.fruitcart.entity;

import com.vn.fruitcart.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cart_items")
public class CartItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "price_at_addition", precision = 10, scale = 2, nullable = false)
    private BigDecimal priceAtAddition;

    @Transient
    public BigDecimal getSubtotal() {
        if (this.priceAtAddition != null && this.quantity != null && this.quantity > 0) {
            return this.priceAtAddition.multiply(new BigDecimal(this.quantity));
        }
        return BigDecimal.ZERO;
    }

    public CartItem(Cart cart, ProductVariant productVariant, Integer quantity, BigDecimal priceAtAddition) {
        this.cart = cart;
        this.productVariant = productVariant;
        this.quantity = quantity;
        this.priceAtAddition = priceAtAddition;
    }
}