package com.vn.fruitcart.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.vn.fruitcart.entity.Category;
import com.vn.fruitcart.entity.dto.request.CategoryReq;
import com.vn.fruitcart.exception.ResourceNotFoundException;
import com.vn.fruitcart.repository.CategoryRepository;
import com.vn.fruitcart.service.specification.CategorySpecification;
import com.vn.fruitcart.util.StringUtil;

import groovyjarjarpicocli.CommandLine.DuplicateNameException;
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
        category.setStatus(cReq.getStatus());

        Category savedCategory = categoryRepository.save(category);
        return savedCategory;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public Page<Category> getAllCategoriesWithFiltersAndPagination(String nameKeyword, Boolean status, int page,
            int size, String sortField, String sortDir) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(direction, sortField));

        Specification<Category> spec = Specification.where(null);

        if (nameKeyword != null && !nameKeyword.isEmpty()) {
            spec = spec.and(CategorySpecification.hasName(nameKeyword));
        }

        if (status != null) {
            spec = spec.and(CategorySpecification.hasStatus(status));
        }

        return categoryRepository.findAll(spec, pageable);
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với id: " + id));
    }

    public Category updateCategory(Long id, CategoryReq req) {
        Category existingCategory = getCategoryById(id);

        if (!existingCategory.getName().equalsIgnoreCase(req.getName())
                && categoryRepository.existsByName(req.getName())) {
            throw new DuplicateNameException("Tên danh mục '" + req.getName() + "' đã tồn tại.");
        }

        existingCategory.setName(req.getName());
        existingCategory.setDescription(req.getDescription());
        existingCategory.setStatus(req.getStatus());
        existingCategory.setSlug(StringUtil.toSlug(req.getName()));
        return categoryRepository.save(existingCategory);
    }

    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id + " để xóa.");
        }
        categoryRepository.deleteById(id);
    }

    public List<Category> findAllActiveCategories() {
        Specification<Category> spec = CategorySpecification.hasStatus(true);
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        return categoryRepository.findAll(spec, sort);
    }

}
