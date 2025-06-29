package com.vn.fruitcart.repository;

import com.vn.fruitcart.entity.Discount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {
    Page<Discount> findAll(Specification<Discount> spec, Pageable pageable);
}
