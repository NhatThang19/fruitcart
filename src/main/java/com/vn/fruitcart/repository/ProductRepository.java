package com.vn.fruitcart.repository;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;

import com.vn.fruitcart.entity.Product;

@Repository
public interface ProductRepository extends DataTablesRepository<Product, Long> {
    
}
