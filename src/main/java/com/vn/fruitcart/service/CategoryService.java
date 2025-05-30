package com.vn.fruitcart.service;

import org.springframework.stereotype.Service;

import com.vn.fruitcart.entity.Category;
import com.vn.fruitcart.entity.dto.request.CategoryReq;
import com.vn.fruitcart.repository.CategoryRepository;
import com.vn.fruitcart.util.StringUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Transactional
    public Category createCategory(CategoryReq cReq) {
        if (categoryRepository.findByName(cReq.getName()).isPresent()) {
            throw new IllegalArgumentException("Tên danh mục đã tồn tại.");
        }

        Category category = new Category();
        category.setName(cReq.getName());
        category.setSlug(StringUtil.toSlug(cReq.getName()));
        category.setDescription(cReq.getDescription());
        category.setStatus(cReq.getStatus() != null ? cReq.getStatus() : true);

        Category savedCategory = categoryRepository.save(category);
        return savedCategory;
    }
}
