package com.vn.fruitcart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vn.fruitcart.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

}
