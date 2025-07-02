package com.vn.fruitcart.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.vn.fruitcart.entity.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.vn.fruitcart.entity.dto.request.product.ProductSearchCriteriaReq;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

public class ProductSpecification {
    public static Specification<Product> searchProducts(ProductSearchCriteriaReq criteria) {
        return (Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(criteria.getKeyword())) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + criteria.getKeyword().toLowerCase() + "%"));
            }

            if (criteria.getCategoryId() != null && criteria.getCategoryId() > 0) {
                Join<Product, Category> categoryJoin = root.join("category", JoinType.INNER);
                predicates.add(cb.equal(categoryJoin.get("id"), criteria.getCategoryId()));
            }

            if (criteria.getOriginId() != null && criteria.getOriginId() > 0) {
                Join<Product, Origin> originJoin = root.join("origin", JoinType.INNER);
                predicates.add(cb.equal(originJoin.get("id"), criteria.getOriginId()));
            }

            if (criteria.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Product> byKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isEmpty()) {
                return cb.conjunction();
            }
            String lowercaseKeyword = "%" + keyword.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("name")), lowercaseKeyword),
                    cb.like(cb.lower(root.get("description")), lowercaseKeyword));
        };
    }

    public static Specification<Product> byCategorySlug(String categorySlug) {
        return (root, query, cb) -> {
            if (categorySlug == null || categorySlug.isEmpty()) {
                return cb.conjunction();
            }

            Join<Product, Category> categoryJoin = root.join("category");
            return cb.equal(categoryJoin.get("slug"), categorySlug);
        };
    }

    public static Specification<Product> byOriginSlug(String originSlug) {
        return (root, query, cb) -> {
            if (originSlug == null || originSlug.isEmpty()) {
                return cb.conjunction();
            }

            Join<Product, Origin> originJoin = root.join("origin");
            return cb.equal(originJoin.get("slug"), originSlug);
        };
    }

    public static Specification<Product> byPriceRange(Double minPrice, Double maxPrice) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            query.distinct(true);
            Join<Product, ProductVariant> variantJoin = root.join("variants");

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(variantJoin.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(variantJoin.get("price"), maxPrice));
            }
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Product> byInStock(boolean inStockOnly) {
        if (!inStockOnly) {

            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> {
            query.distinct(true);

            Subquery<Long> subquery = query.subquery(Long.class);
            Root<ProductVariant> productVariantRoot = subquery.from(ProductVariant.class);
            Join<ProductVariant, Inventory> inventoryJoin = productVariantRoot.join("inventory");

            subquery.select(cb.literal(1L))
                    .where(cb.and(
                            cb.equal(productVariantRoot.get("product"), root),
                            cb.greaterThan(inventoryJoin.get("quantity"), 0)));
            return cb.exists(subquery);
        };
    }

    public static Specification<Product> isActive() {
        return (root, query, cb) -> cb.equal(root.get("status"), true);
    }

    public static Specification<Product> byIsNew(Boolean isNew) {
        if (isNew == null || !isNew) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.isTrue(root.get("isNew"));
    }

    public static Specification<Product> byOnSale(Boolean onSale) {
        if (onSale == null || !onSale) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> {
            query.distinct(true);

            Subquery<Long> subquery = query.subquery(Long.class);
            Root<ProductVariant> variantRoot = subquery.from(ProductVariant.class);
            Join<ProductVariant, Discount> discountJoin = variantRoot.join("discounts", JoinType.INNER);

            LocalDateTime now = LocalDateTime.now();

            subquery.select(variantRoot.get("product").get("id"))
                    .where(
                            cb.isTrue(discountJoin.get("active")),
                            cb.lessThanOrEqualTo(discountJoin.get("startDate"), now),
                            cb.greaterThanOrEqualTo(discountJoin.get("endDate"), now)
                    );

            return root.get("id").in(subquery);
        };
    }
}
