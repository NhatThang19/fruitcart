package com.vn.fruitcart.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.vn.fruitcart.entity.Category;
import com.vn.fruitcart.entity.ProductVariant;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {
    Optional<Category> findByName(String name);

    Optional<Category> findBySlug(String slug);

    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Optional<Category> findById(Long id);

    boolean existsByName(String name);

    Optional<Category> findByIdAndIsDeletedFalse(Long categoryId);

}
