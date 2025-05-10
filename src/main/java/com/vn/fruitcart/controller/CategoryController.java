package com.vn.fruitcart.controller;

import org.springframework.stereotype.Controller;

import com.vn.fruitcart.service.CategoryService;

@Controller
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }
}
