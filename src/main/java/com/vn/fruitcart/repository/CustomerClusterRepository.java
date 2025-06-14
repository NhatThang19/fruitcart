package com.vn.fruitcart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vn.fruitcart.entity.CustomerCluster;
import com.vn.fruitcart.entity.User;

@Repository
public interface CustomerClusterRepository extends JpaRepository<CustomerCluster, Long> {
    Optional<CustomerCluster> findByUser(User user);

    Optional<CustomerCluster> findFirstByOrderByAssignedDateDesc();
}
