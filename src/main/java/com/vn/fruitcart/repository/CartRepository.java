package com.vn.fruitcart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vn.fruitcart.entity.Cart;
import com.vn.fruitcart.entity.User;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long>  {
    Optional<Cart> findByUser(User user);

    Optional<Cart> findByUserId(Long userId);
}
