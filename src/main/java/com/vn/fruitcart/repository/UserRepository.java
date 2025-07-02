package com.vn.fruitcart.repository;

import com.vn.fruitcart.entity.User;

import java.util.List;
import java.util.Optional;

import com.vn.fruitcart.entity.dto.ClusterUserCountDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    @Query("SELECT cc.clusterNumber, COUNT(u.id) " +
            "FROM User u JOIN u.customerCluster cc " +
            "GROUP BY cc.clusterNumber " +
            "ORDER BY COUNT(u.id) DESC")
    List<Object[]> countUsersByClusterNumber();

}
