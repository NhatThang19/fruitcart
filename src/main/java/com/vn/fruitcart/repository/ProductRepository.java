package com.vn.fruitcart.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.vn.fruitcart.entity.dto.BestSellingProductDto;
import com.vn.fruitcart.entity.dto.LowStockProductDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vn.fruitcart.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Product> findByOriginId(Long originId, Pageable pageable);

    Optional<Product> findBySlug(String slug);

    @Query("SELECT new com.vn.fruitcart.entity.dto.BestSellingProductDto(p, SUM(oi.quantity)) " +
            "FROM Product p JOIN p.variants pv JOIN pv.orderItems oi " +
            "GROUP BY p.id " +
            "ORDER BY SUM(oi.quantity) DESC")
    Page<BestSellingProductDto> findBestSellingProducts(Pageable pageable);

    @Query("SELECT new com.vn.fruitcart.entity.dto.LowStockProductDto(p, SUM(i.quantity)) " +
            "FROM Product p JOIN p.variants pv JOIN pv.inventory i " +
            "GROUP BY p.id " +
            "HAVING SUM(i.quantity) < :stockThreshold")
    List<LowStockProductDto> findLowStockProducts(@Param("stockThreshold") int stockThreshold);

    @Query("SELECT DISTINCT p FROM Product p " +
            "JOIN p.variants pv " +
            "JOIN pv.discounts d " +
            "WHERE d.active = true " +
            "AND d.startDate <= :now " +
            "AND d.endDate >= :now")
    List<Product> findOnSaleProducts(LocalDateTime now);
}
