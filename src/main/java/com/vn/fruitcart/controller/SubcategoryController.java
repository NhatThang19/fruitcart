package com.vn.fruitcart.controller;

import org.springframework.stereotype.Controller;

import com.vn.fruitcart.service.SubCategoryService;

@Controller
public class SubcategoryController {
    private final SubCategoryService subCategoryService;

    public SubcategoryController(SubCategoryService subCategoryService) {
        this.subCategoryService = subCategoryService;
    }
}
