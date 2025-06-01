package com.vn.fruitcart.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vn.fruitcart.entity.PurchaseOrderItem;

public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, Long> {

}
