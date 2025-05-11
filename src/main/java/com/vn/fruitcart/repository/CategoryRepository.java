package com.vn.fruitcart.repository;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;

import com.vn.fruitcart.entity.Category;

@Repository
public interface CategoryRepository extends DataTablesRepository<Category, Long> {
    boolean existsBySlug(String slug);

}
