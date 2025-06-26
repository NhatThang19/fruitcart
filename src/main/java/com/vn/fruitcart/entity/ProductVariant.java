package com.vn.fruitcart.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.vn.fruitcart.entity.base.BaseEntity;
import com.vn.fruitcart.util.FruitCartUtils;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
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

}
