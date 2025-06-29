package com.vn.fruitcart.service;

import com.vn.fruitcart.entity.Discount;
import com.vn.fruitcart.entity.ProductVariant;
import com.vn.fruitcart.entity.dto.request.DiscountReq;
import com.vn.fruitcart.entity.dto.request.DiscountSearchCriteriaReq;
import com.vn.fruitcart.exception.ResourceNotFoundException;
import com.vn.fruitcart.repository.DiscountRepository;
import com.vn.fruitcart.repository.ProductVariantRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscountService {
    private final DiscountRepository discountRepository;
    private final ProductVariantRepository variantRepository;

    public List<Discount> findAll() {
        return discountRepository.findAll();
    }

    public Discount findById(Long id) {
        return discountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chương trình giảm giá với ID: " + id));
    }

    public List<ProductVariant> findAllVariants() {
        return variantRepository.findAll();
    }

    @Transactional
    public void saveFromRequest(DiscountReq req) {
        // Tìm hoặc tạo mới một đối tượng Discount entity
        Discount discount = req.getId() != null ? findById(req.getId()) : new Discount();

        // Map dữ liệu từ DTO sang Entity
        discount.setName(req.getName());
        discount.setDiscountPercentage(req.getDiscountPercentage());
        discount.setStartDate(req.getStartDate());
        discount.setEndDate(req.getEndDate());
        discount.setActive(req.isActive());

        // 3. Xóa tất cả các mối quan hệ cũ một cách an toàn
        // Tạo một bản sao để tránh ConcurrentModificationException khi xóa
        List<ProductVariant> oldVariants = new ArrayList<>(discount.getVariants());
        for (ProductVariant oldVariant : oldVariants) {
            discount.removeVariant(oldVariant);
        }

        // 4. Thêm các mối quan hệ mới
        if (req.getVariantIds() != null) {
            for (Long variantId : req.getVariantIds()) {
                variantRepository.findById(variantId).ifPresent(discount::addVariant);
            }
        }

        discountRepository.save(discount);
    }

    @Transactional
    public void deleteById(Long id) {
        discountRepository.findById(id).ifPresent(discount -> {
            discount.getVariants().clear();
            discountRepository.save(discount);

            discountRepository.delete(discount);
        });
    }

    public Page<Discount> findByCriteria(DiscountSearchCriteriaReq criteria, Pageable pageable) {
        Specification<Discount> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(criteria.getName())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
                        "%" + criteria.getName().toLowerCase() + "%"));
            }

            if (criteria.getActive() != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), criteria.getActive()));
            }

            if (criteria.getStartDateAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"),
                        criteria.getStartDateAfter().atStartOfDay()));
            }


            if (criteria.getEndDateBefore() != null) {
                predicates.add(criteriaBuilder.lessThan(root.get("endDate"),
                        criteria.getEndDateBefore().plusDays(1).atStartOfDay()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return discountRepository.findAll(spec, pageable);
    }
}
