package com.vn.fruitcart.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import com.vn.fruitcart.util.constant.CustomerClusterEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "customer_clusters")
public class CustomerCluster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "cluster_number", nullable = false)
    private int clusterNumber;

    @CreationTimestamp
    @Column(name = "assigned_date", updatable = false)
    private Instant assignedDate;

    public CustomerCluster(User user, int clusterNumber) {
        this.user = user;
        this.clusterNumber = clusterNumber;
    }

    @Transient
    public CustomerClusterEnum getClusterEnum() {
        return CustomerClusterEnum.fromClusterNumber(this.clusterNumber);
    }

    @Transient
    public String getClusterName() {
        return getClusterEnum().getClusterName();
    }

    @Transient
    public String getClusterDescription() {
        return getClusterEnum().getDescription();
    }
}
