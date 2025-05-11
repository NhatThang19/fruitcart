package com.vn.fruitcart.repository;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;

import com.vn.fruitcart.entity.SubCategory;

@Repository
public interface SubCategoryRepository extends DataTablesRepository<SubCategory, Long> {

}
