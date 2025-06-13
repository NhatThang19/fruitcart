package com.vn.fruitcart.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vn.fruitcart.entity.Order;
import com.vn.fruitcart.entity.User;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.user = :user")

    Optional<BigDecimal> findTotalSpendingByUser(@Param("user") User user);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user = :user AND o.createdDate >= :startDate")
    long countByUserAndCreatedDateAfter(@Param("user") User user, @Param("startDate") Instant startDate); 

    @Query("SELECT MAX(o.createdDate) FROM Order o WHERE o.user = :user")
    Optional<Instant> findMostRecentOrderDateByUser(@Param("user") User user); 
}
