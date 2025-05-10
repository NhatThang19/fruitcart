package com.vn.fruitcart.service;

import org.springframework.stereotype.Service;

import com.vn.fruitcart.repository.SubCategoryRepository;

@Service
public class SubCategoryService {
    private final SubCategoryRepository subCategoryRepository;

    public SubCategoryService(SubCategoryRepository subCategoryRepository) {
        this.subCategoryRepository = subCategoryRepository;
    }
}
