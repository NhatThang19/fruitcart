package com.vn.fruitcart.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.vn.fruitcart.entity.PurchaseOrder;
import com.vn.fruitcart.util.constant.PurchaseOrderStatusEnum;

public interface PurchaseOrderRepository
        extends JpaRepository<PurchaseOrder, Long>, JpaSpecificationExecutor<PurchaseOrder> {

    Page<PurchaseOrder> findByStatus(PurchaseOrderStatusEnum statusFilter, Pageable pageable);

}
