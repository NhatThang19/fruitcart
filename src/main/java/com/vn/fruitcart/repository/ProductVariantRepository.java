package com.vn.fruitcart.repository;

import com.vn.fruitcart.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    // Optional<ProductVariant> findBySku(String sku);
    // boolean existsBySku(String sku);
}