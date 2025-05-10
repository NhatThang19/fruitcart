package com.vn.fruitcart.service;

import org.springframework.stereotype.Service;

import com.vn.fruitcart.repository.CategoryRepository;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;   
    }
}
