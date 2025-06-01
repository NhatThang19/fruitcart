package com.vn.fruitcart.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.vn.fruitcart.entity.InventoryAudit;
import com.vn.fruitcart.entity.ProductVariant;
import com.vn.fruitcart.exception.ResourceNotFoundException;
import com.vn.fruitcart.repository.InventoryAuditRepository;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryAuditService {
    private final InventoryAuditRepository inventoryAuditRepository;

    public Page<InventoryAudit> listInventoryAudits(Long productVariantIdFilter,
            LocalDate dateFromFilter,
            LocalDate dateToFilter,
            Pageable pageable) {

        Specification<InventoryAudit> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (productVariantIdFilter != null) {
                Join<InventoryAudit, ProductVariant> productVariantJoin = root.join("productVariant"); //
                predicates.add(criteriaBuilder.equal(productVariantJoin.get("id"), productVariantIdFilter));
            }

            if (dateFromFilter != null) {
                Instant startOfDay = dateFromFilter.atStartOfDay().toInstant(ZoneOffset.UTC);
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"), startOfDay));
            }

            if (dateToFilter != null) {
                Instant endOfDay = dateToFilter.atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdDate"), endOfDay));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return inventoryAuditRepository.findAll(spec, pageable);
    }

    public InventoryAudit findById(Long auditId) {
        return inventoryAuditRepository.findById(auditId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản ghi kiểm kho với ID: " + auditId)); 
    }
}
