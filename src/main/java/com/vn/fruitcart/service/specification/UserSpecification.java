package com.vn.fruitcart.service.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.vn.fruitcart.entity.Role;
import com.vn.fruitcart.entity.User;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

public class UserSpecification {
    public static Specification<User> emailContains(String email) {
        return (root, query, criteriaBuilder) -> {
            if (email == null || email.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + email.toLowerCase() + "%");
        };
    }

    public static Specification<User> hasRoleId(Long roleId) {
        return (root, query, criteriaBuilder) -> {
            if (roleId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<User, Role> roleJoin = root.join("role");
            return criteriaBuilder.equal(roleJoin.get("id"), roleId);
        };
    }

    public static Specification<User> isBlocked(Boolean isBlocked) {
        return (root, query, criteriaBuilder) -> {
            if (isBlocked == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("isBlocked"), isBlocked);
        };
    }

    public static Specification<User> filterBy(String email, Long roleId, Boolean isBlocked) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (email != null && !email.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("email")),
                        "%" + email.toLowerCase() + "%"));
            }

            if (roleId != null) {
                Join<User, Role> roleJoin = root.join("role");
                predicates.add(criteriaBuilder.equal(roleJoin.get("id"), roleId));
            }

            if (isBlocked != null) {
                predicates.add(criteriaBuilder.equal(root.get("isBlocked"), isBlocked));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
