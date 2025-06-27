package com.vn.fruitcart.service;

import com.vn.fruitcart.entity.Inventory;
import com.vn.fruitcart.entity.InventoryAudit;
import com.vn.fruitcart.entity.ProductVariant;
import com.vn.fruitcart.entity.dto.request.inventory.InventoryAuditSearchCriteriaReq;
import com.vn.fruitcart.entity.dto.request.inventory.StocktakeItemReq;
import com.vn.fruitcart.entity.dto.request.inventory.StocktakeReq;
import com.vn.fruitcart.exception.ResourceNotFoundException;
import com.vn.fruitcart.repository.InventoryAuditRepository;
import com.vn.fruitcart.repository.InventoryRepository;
import com.vn.fruitcart.repository.ProductVariantRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryAuditRepository inventoryAuditRepository;
    private final ProductVariantRepository productVariantRepository;


    @Transactional
    public void updateInventoryAndLogAudit(ProductVariant productVariant, int quantityChange, String reason) {
        if (productVariant == null) {
            throw new IllegalArgumentException("ProductVariant không được để trống khi cập nhật tồn kho.");
        }

        Inventory inventory = inventoryRepository.findByProductVariant(productVariant)
                .orElseGet(() -> {
                    Inventory newInventory = new Inventory();
                    newInventory.setProductVariant(productVariant);
                    newInventory.setQuantity(0);
                    return newInventory;
                });

        int systemQuantityBeforeChange = inventory.getQuantity();
        int newSystemQuantity = systemQuantityBeforeChange + quantityChange;

        if (newSystemQuantity < 0) {
            throw new IllegalArgumentException("Số lượng tồn kho không thể âm cho sản phẩm: " + productVariant.getSku()
                    + ". Số lượng hiện tại: " + systemQuantityBeforeChange + ", yêu cầu thay đổi: " + quantityChange);
        }

        inventory.setQuantity(newSystemQuantity);
        inventoryRepository.save(inventory);

        InventoryAudit audit = new InventoryAudit();
        audit.setProductVariant(productVariant);
        audit.setSystemQuantity(systemQuantityBeforeChange);
        audit.setActualQuantity(newSystemQuantity);
        audit.setAdjustment(quantityChange);
        audit.setTakeNote(reason);

        inventoryAuditRepository.save(audit);
    }

    @Transactional
    public void performMultiProductStocktake(StocktakeReq stocktakeRequest) {
        if (stocktakeRequest == null || stocktakeRequest.getItems() == null || stocktakeRequest.getItems().isEmpty()) {
            throw new IllegalArgumentException("Không có sản phẩm nào được cung cấp để kiểm kê.");
        }

        long distinctProductCount = stocktakeRequest.getItems().stream()
                .map(StocktakeItemReq::getProductVariantId)
                .distinct()
                .count();
        if (distinctProductCount < stocktakeRequest.getItems().size()) {
            throw new IllegalArgumentException("Không thể kiểm kê cùng một sản phẩm nhiều lần trong một phiếu.");
        }

        for (StocktakeItemReq item : stocktakeRequest.getItems()) {
            ProductVariant variant = productVariantRepository.findById(item.getProductVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy biến thể sản phẩm với ID: " + item.getProductVariantId()));

            Inventory inventory = variant.getInventory();
            if (inventory == null) {
                throw new IllegalStateException("Không tìm thấy bản ghi tồn kho cho sản phẩm: " + variant.getSku());
            }

            int systemQuantity = inventory.getQuantity();
            int actualQuantity = item.getActualCountedQuantity();

            // Chỉ cập nhật và ghi log nếu có sự thay đổi
            if (systemQuantity != actualQuantity) {
                inventory.setQuantity(actualQuantity);
                inventoryRepository.save(inventory);

                InventoryAudit audit = new InventoryAudit();
                audit.setProductVariant(variant);
                audit.setSystemQuantity(systemQuantity);
                audit.setActualQuantity(actualQuantity);
                audit.setTakeNote(stocktakeRequest.getStocktakeNote());

                inventoryAuditRepository.save(audit);
            }
        }
    }

    public Page<InventoryAudit> listInventoryAudits(Long productVariantIdFilter,
                                                    LocalDate dateFromFilter,
                                                    LocalDate dateToFilter,
                                                    Pageable pageable) {

        Specification<InventoryAudit> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (productVariantIdFilter != null) {
                Join<InventoryAudit, ProductVariant> productVariantJoin = root.join("productVariant");
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

    public InventoryAudit findAuditById(Long auditId) {
        return inventoryAuditRepository.findById(auditId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản ghi kiểm kho với ID: " + auditId));
    }

    public Page<InventoryAudit> listInventoryAudits(InventoryAuditSearchCriteriaReq criteria, Pageable pageable) {

        Specification<InventoryAudit> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getProductVariantId() != null) {
                Join<InventoryAudit, ProductVariant> productVariantJoin = root.join("productVariant");
                predicates.add(criteriaBuilder.equal(productVariantJoin.get("id"), criteria.getProductVariantId()));
            }

            if (criteria.getDateFrom() != null) {
                Instant startOfDay = criteria.getDateFrom().atStartOfDay().toInstant(ZoneOffset.UTC);
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"), startOfDay));
            }

            if (criteria.getDateTo() != null) {
                Instant endOfDay = criteria.getDateTo().atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdDate"), endOfDay));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return inventoryAuditRepository.findAll(spec, pageable);
    }
}