package com.vn.fruitcart.controller.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vn.fruitcart.entity.Category;
import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.repository.CategoryRepository;
import com.vn.fruitcart.repository.UserRepository;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

@RestController
public class DatatablesAPI {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/api/users")
    public DataTablesOutput<User> getUsers(@Valid DataTablesInput input) {
        return userRepository.findAll(input);
    }

    @GetMapping("/api/categories")
    public DataTablesOutput<Category> getCategories(@Valid DataTablesInput input) {
        return categoryRepository.findAll(input);
    }

}
