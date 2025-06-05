package com.vn.fruitcart.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vn.fruitcart.entity.Cart;
import com.vn.fruitcart.entity.CartItem;
import com.vn.fruitcart.entity.ProductVariant;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProductVariant(Cart cart, ProductVariant productVariant);

    List<CartItem> findByCart(Cart cart);

    void deleteByCartId(Long id);
}