package com.vn.fruitcart.service;

import org.springframework.stereotype.Service;

import com.vn.fruitcart.entity.Product;
import com.vn.fruitcart.repository.ProductRepository;
import com.vn.fruitcart.repository.SubCategoryRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final SubCategoryRepository subcategoryRepository;

    public ProductService(ProductRepository productRepository, SubCategoryRepository subcategoryRepository) {
        this.productRepository = productRepository;
        this.subcategoryRepository = subcategoryRepository;
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public Product findById(Long id) {
        return this.productRepository.findById(id).orElse(null);
    }
}