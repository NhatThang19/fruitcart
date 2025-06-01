package com.vn.fruitcart.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.vn.fruitcart.entity.base.BaseEntity;
import com.vn.fruitcart.util.constant.PurchaseOrderStatusEnum;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
@Table(name = "purchase_orders")
public class PurchaseOrder extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private PurchaseOrderStatusEnum status = PurchaseOrderStatusEnum.PENDING;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PurchaseOrderItem> purchaseOrderItems = new ArrayList<>();

    public void addPurchaseOrderItem(PurchaseOrderItem item) {
        purchaseOrderItems.add(item);
        item.setPurchaseOrder(this);
    }

    public void removePurchaseOrderItem(PurchaseOrderItem item) {
        purchaseOrderItems.remove(item);
        item.setPurchaseOrder(null);
    }
}
