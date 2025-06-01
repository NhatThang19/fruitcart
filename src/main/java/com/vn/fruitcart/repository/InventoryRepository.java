package com.vn.fruitcart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vn.fruitcart.entity.Inventory;
import com.vn.fruitcart.entity.ProductVariant;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductVariant(ProductVariant productVariant);

    Optional<Inventory> findByProductVariantId(Long productVariantId);

}
