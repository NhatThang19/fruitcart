package com.vn.fruitcart.entity;

import com.vn.fruitcart.entity.base.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "inventory_audits")
public class InventoryAudit extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @Column(name = "actual_quantity", nullable = false)
    private Integer actualQuantity;

    @Column(name = "system_quantity", nullable = false)
    private Integer systemQuantity;

    @Column(name = "adjustment")
    private Integer adjustment;

    @Column(name = "take_note", columnDefinition = "TEXT")
    private String takeNote;

    @PrePersist
    protected void onCreateAudit() {
        if (this.adjustment == null) {
            this.adjustment = this.actualQuantity - this.systemQuantity;
        }
    }
}
