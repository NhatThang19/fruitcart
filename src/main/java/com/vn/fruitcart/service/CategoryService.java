package com.vn.fruitcart.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.vn.fruitcart.entity.Category;
import com.vn.fruitcart.repository.CategoryRepository;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public String generateUniqueSlug(String baseSlug) {
        String slug = baseSlug;
        int counter = 1;

        while (categoryRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }
        return slug;
    }

    public void save(Category category) {
        categoryRepository.save(category);
    }

    public void deleteById(Long id) {
        categoryRepository.deleteById(id);
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    public List<Category> findAllParentCategories() {
        return categoryRepository.findAll();
    }

    public List<Category> findAllActiveCategories() {
        return categoryRepository.findByActiveTrueOrderByNameAsc();
    }
}
