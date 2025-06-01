package com.vn.fruitcart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.vn.fruitcart.entity.InventoryAudit;

@Repository
public interface InventoryAuditRepository extends JpaRepository<InventoryAudit, Long>, JpaSpecificationExecutor<InventoryAudit> {

}
