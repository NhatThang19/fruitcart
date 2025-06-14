package com.vn.fruitcart.service.specification;

import org.springframework.data.jpa.domain.Specification;

import com.vn.fruitcart.entity.Order;
import com.vn.fruitcart.util.constant.OrderStatusEnum;

public class OrderSpecification {
    public static Specification<Order> hasName(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("createdBy")), "%" + name.toLowerCase() + "%");
        };
    }

public static Specification<Order> hasStatus(OrderStatusEnum status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction(); 
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }
}
