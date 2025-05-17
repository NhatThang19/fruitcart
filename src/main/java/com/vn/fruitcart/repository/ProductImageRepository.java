package com.vn.fruitcart.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vn.fruitcart.entity.ProductImage;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductId(Long productId);

    Optional<ProductImage> findByProductIdAndIsMain(Long productId, boolean isMain);

    Optional<ProductImage> findByProductIdAndIsMainTrue(Long productIds);
}
