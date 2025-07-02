package com.vn.fruitcart.controller;

import com.vn.fruitcart.entity.Category;
import com.vn.fruitcart.entity.Origin;
import com.vn.fruitcart.service.CategoryService;
import com.vn.fruitcart.service.OriginService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;


@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final CategoryService categoryService;
    private final OriginService originService;


    @ModelAttribute("currentUri")
    public String getCurrentUri(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ModelAttribute("headerCategories")
    public List<Category> getHeaderCategories() {
        return categoryService.findAllActiveCategories().stream().limit(5).toList();
    }

    @ModelAttribute("headerOrigins")
    public List<Origin> getHeaderOrigins() {
        return originService.findAllActiveOrigins().stream().limit(5).toList();
    }

}