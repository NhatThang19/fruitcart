package com.vn.fruitcart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.vn.fruitcart.entity.Origin;

@Repository
public interface OriginRepository extends JpaRepository<Origin, Long>, JpaSpecificationExecutor<Origin> {
    Optional<Origin> findByName(String name);

    boolean existsByName(String name);

}
