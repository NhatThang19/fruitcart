package com.vn.fruitcart.service;

import org.springframework.stereotype.Service;

import com.vn.fruitcart.entity.Inventory;
import com.vn.fruitcart.entity.InventoryAudit;
import com.vn.fruitcart.entity.ProductVariant;
import com.vn.fruitcart.entity.PurchaseOrder;
import com.vn.fruitcart.exception.ResourceNotFoundException;
import com.vn.fruitcart.repository.InventoryAuditRepository;
import com.vn.fruitcart.repository.InventoryRepository;
import com.vn.fruitcart.repository.ProductVariantRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final InventoryAuditRepository inventoryAuditRepository;
    private final ProductVariantRepository productVariantRepository;

    @Transactional
    public void updateInventoryAndLogAudit(ProductVariant productVariant, int quantityChange, String reason,
            PurchaseOrder purchaseOrder) {
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

    @Transactional()
    public int getStockQuantity(Long productVariantId) {
        ProductVariant productVariant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy ProductVariant với ID: " + productVariantId));

        return inventoryRepository.findByProductVariant(productVariant)
                .map(Inventory::getQuantity)
                .orElse(0);
    }
}
