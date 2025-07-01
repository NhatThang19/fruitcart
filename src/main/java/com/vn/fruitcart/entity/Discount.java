package com.vn.fruitcart.entity;

import com.vn.fruitcart.util.constant.CustomerClusterEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "discounts")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Discount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private boolean active = true;

    @ManyToMany(mappedBy = "discounts")
    private List<ProductVariant> variants = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "target_customer_cluster")
    private CustomerClusterEnum targetCluster;

    public void addVariant(ProductVariant variant) {
        this.variants.add(variant);
        variant.getDiscounts().add(this);
    }

    public void removeVariant(ProductVariant variant) {
        this.variants.remove(variant);
        variant.getDiscounts().remove(this);
    }

}
