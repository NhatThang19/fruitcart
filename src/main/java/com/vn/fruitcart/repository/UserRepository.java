package com.vn.fruitcart.repository;

import com.vn.fruitcart.entity.User;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository
    extends DataTablesRepository<User, Long> {

  User getUserByEmail(String email);

  boolean existsByEmail(String email);
}
