package com.vn.fruitcart.controller.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vn.fruitcart.entity.Category;
import com.vn.fruitcart.entity.SubCategory;
import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.entity.dto.response.CategoryResDTO;
import com.vn.fruitcart.repository.CategoryRepository;
import com.vn.fruitcart.repository.SubCategoryRepository;
import com.vn.fruitcart.repository.UserRepository;

import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

@RestController
public class DatatablesAPI {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SubCategoryRepository subCategoryRepository;

    @GetMapping("/api/users")
    public DataTablesOutput<User> getUsers(@Valid DataTablesInput input) {
        return userRepository.findAll(input);
    }

    @GetMapping("/api/categories")
    public DataTablesOutput<CategoryResDTO> getCategories(@Valid DataTablesInput input) {
        input.getColumns().forEach(column -> {
            if (column.getData().equals("subCategoryCount") ||
                    column.getData().equals("subCategoryNames")) {
                column.setSearchable(false);
                column.setOrderable(false);
            }
        });

        DataTablesOutput<Category> categories = categoryRepository.findAll(input);

        DataTablesOutput<CategoryResDTO> output = new DataTablesOutput<>();
        output.setDraw(categories.getDraw());
        output.setRecordsTotal(categories.getRecordsTotal());
        output.setRecordsFiltered(categories.getRecordsFiltered());

        List<CategoryResDTO> dtos = categories.getData().stream()
                .map(category -> {
                    String subNames = category.getSubCategories().stream()
                            .map(SubCategory::getName)
                            .collect(Collectors.joining(", "));

                    return CategoryResDTO.builder()
                            .id(category.getId())
                            .name(category.getName())
                            .slug(category.getSlug())
                            .active(category.isActive())
                            .subCategoryCount(category.getSubCategories().size())
                            .subCategoryNames(subNames)
                            .lastModifiedDate(category.getLastModifiedDate())
                            .build();
                })
                .collect(Collectors.toList());

        output.setData(dtos);
        return output;
    }

    @GetMapping("/api/subcategories")
    public DataTablesOutput<SubCategory> getSubCategories(@Valid DataTablesInput input) {
        return subCategoryRepository.findAll(input);
    }
}
