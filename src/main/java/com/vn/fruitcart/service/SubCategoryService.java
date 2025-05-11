package com.vn.fruitcart.service;

import org.springframework.stereotype.Service;

import com.vn.fruitcart.entity.SubCategory;
import com.vn.fruitcart.repository.SubCategoryRepository;

@Service
public class SubCategoryService {
    private final SubCategoryRepository subCategoryRepository;

    public SubCategoryService(SubCategoryRepository subCategoryRepository) {
        this.subCategoryRepository = subCategoryRepository;
    }

    public void save(SubCategory subcategory) {
        subCategoryRepository.save(subcategory);
    }

    public void deleteById(Long id) {
        subCategoryRepository.deleteById(id);
    }

    public SubCategory getSubCategoryById(Long id) {
        return subCategoryRepository.findById(id).orElse(null);
    }
}
