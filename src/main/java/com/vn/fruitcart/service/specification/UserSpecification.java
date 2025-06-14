package com.vn.fruitcart.service.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.vn.fruitcart.entity.CustomerCluster;
import com.vn.fruitcart.entity.Role;
import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.entity.dto.request.user.UserSearchCriteriaReq;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

public class UserSpecification {

    public static Specification<User> filterBy(UserSearchCriteriaReq criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            String email = criteria.getEmail();
            if (email != null && !email.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("email")),
                        "%" + email.toLowerCase() + "%"));
            }

            Integer roleId = criteria.getRole();
            if (roleId != null) {
                Join<User, Role> roleJoin = root.join("role");
                predicates.add(criteriaBuilder.equal(roleJoin.get("id"), roleId.longValue()));
            }

            Boolean isBlocked = criteria.getIsBlocked();
            if (isBlocked != null) {
                predicates.add(criteriaBuilder.equal(root.get("isBlocked"), isBlocked));
            }

            Integer clusterNumber = criteria.getClusterNumber();
            if (clusterNumber != null) {

                Join<User, CustomerCluster> clusterJoin = root.join("customerCluster");

                predicates.add(criteriaBuilder.equal(clusterJoin.get("clusterNumber"), clusterNumber));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
