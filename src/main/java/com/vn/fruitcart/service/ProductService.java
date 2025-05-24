package com.vn.fruitcart.service;

import org.springframework.stereotype.Service;

import com.vn.fruitcart.entity.Product;
import com.vn.fruitcart.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public Product findById(Long id) {
        return this.productRepository.findById(id).orElse(null);
    }
}