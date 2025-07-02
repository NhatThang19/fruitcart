package com.vn.fruitcart.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.vn.fruitcart.util.constant.EOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

    Page<Order> findAll(Specification<Order> spec, Pageable pageable);

    Page<Order> findByUser(User user, Pageable pageable);

    Optional<Order> findByIdAndUser(Long id, User user);

    @Query("SELECT FUNCTION('DATE_FORMAT', o.createdDate, '%Y-%m'), SUM(o.totalAmount) " +
            "FROM Order o " +
            "WHERE o.status = com.vn.fruitcart.util.constant.EOrderStatus.COMPLETED " +
            "AND o.createdDate >= :startDate " +
            "GROUP BY FUNCTION('DATE_FORMAT', o.createdDate, '%Y-%m') " +
            "ORDER BY FUNCTION('DATE_FORMAT', o.createdDate, '%Y-%m') ASC")
    List<Object[]> findMonthlySalesSince(@Param("startDate") Instant startDate);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = :status")
    BigDecimal sumTotalAmountByStatus(@Param("status") EOrderStatus status);

    List<Order> findTop5ByStatusOrderByCreatedDateDesc(EOrderStatus status);
}
