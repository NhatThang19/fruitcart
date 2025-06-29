package com.vn.fruitcart.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

import com.vn.fruitcart.entity.base.BaseEntity;
import com.vn.fruitcart.util.FruitCartUtils;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_variants")
public class ProductVariant extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku", nullable = false, unique = true)
    private String sku;

    @Column(name = "price", nullable = false, precision = 12)
    private BigDecimal price;

    @Column(name = "attribute", nullable = false)
    private String attribute;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @OneToOne(mappedBy = "productVariant", cascade = CascadeType.ALL, orphanRemoval = true)
    private Inventory inventory;

    @OneToMany(mappedBy = "productVariant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InventoryAudit> inventoryAudits = new ArrayList<>();

    @OneToMany(mappedBy = "productVariant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToMany(mappedBy = "productVariant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PurchaseOrderItem> purchaseOrdersItems = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "variant_discounts",
            joinColumns = @JoinColumn(name = "variant_id"),
            inverseJoinColumns = @JoinColumn(name = "discount_id")
    )
    @OrderColumn(name = "discount_order")
    private List<Discount> discounts = new ArrayList<>();

    @PrePersist
    public void generateSku() {
        if (this.sku == null || this.sku.trim().isEmpty()) {
            if (this.product != null && this.product.getName() != null) {
                String productSlug = FruitCartUtils.toSlug(this.product.getName());

                String attributeSLug = FruitCartUtils.toSlug(this.attribute);

                String uniqueId = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

                this.sku = String.format("%s-%s-%s", productSlug, attributeSLug, uniqueId).toUpperCase();
            }
        }

        if (this.inventory == null) {
            Inventory newInventory = new Inventory();
            newInventory.setQuantity(0);
            newInventory.setProductVariant(this);
            this.setInventory(newInventory);
        }
    }

    public Optional<Discount> getActiveDiscount() {
        LocalDateTime now = LocalDateTime.now();

        return this.discounts.stream()
                .filter(Discount::isActive)
                .filter(d -> (d.getStartDate() == null || now.isAfter(d.getStartDate())) &&
                        (d.getEndDate() == null || now.isBefore(d.getEndDate())))
                .max(Comparator.comparing(Discount::getDiscountPercentage));
    }

    public BigDecimal getSalePrice() {
        return getActiveDiscount().map(discount -> {
            BigDecimal percentage = discount.getDiscountPercentage();
            BigDecimal discountFactor = BigDecimal.ONE.subtract(percentage.divide(new BigDecimal("100")));
            return this.price.multiply(discountFactor).setScale(2, RoundingMode.HALF_UP);
        }).orElse(this.price);
    }

    public boolean isOnSale() {
        return getActiveDiscount().isPresent();
    }

    public BigDecimal getDiscountAmount() {
        if (!isOnSale()) {
            return BigDecimal.ZERO;
        }
        return this.price.subtract(getSalePrice());
    }

    public String getDisplayableDiscount() {
        return getActiveDiscount()
                .map(discount -> "-" + discount.getDiscountPercentage().stripTrailingZeros().toPlainString() + "%")
                .orElse("");
    }


}
