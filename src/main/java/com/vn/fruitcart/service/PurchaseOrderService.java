package com.vn.fruitcart.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.vn.fruitcart.entity.ProductVariant;
import com.vn.fruitcart.entity.PurchaseOrder;
import com.vn.fruitcart.entity.PurchaseOrderItem;
import com.vn.fruitcart.entity.dto.request.PurchaseOrderCreateReq;
import com.vn.fruitcart.entity.dto.request.PurchaseOrderItemReq;
import com.vn.fruitcart.exception.ResourceNotFoundException;
import com.vn.fruitcart.repository.ProductVariantRepository;
import com.vn.fruitcart.repository.PurchaseOrderRepository;
import com.vn.fruitcart.util.constant.PurchaseOrderStatusEnum;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ProductVariantRepository productVariantRepository;
    private final InventoryService inventoryService;

    @Transactional
    public PurchaseOrder createPurchaseOrder(PurchaseOrderCreateReq createRequestDTO) {
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setNote(createRequestDTO.getNote());
        purchaseOrder.setStatus(PurchaseOrderStatusEnum.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<PurchaseOrderItem> orderItems = new ArrayList<>();

        for (PurchaseOrderItemReq itemDTO : createRequestDTO.getItems()) {
            ProductVariant productVariant = productVariantRepository.findById(itemDTO.getProductVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ProductVariant với ID: "
                            + itemDTO.getProductVariantId() + " khi tạo đơn nhập hàng."));

            PurchaseOrderItem orderItem = new PurchaseOrderItem();
            orderItem.setProductVariant(productVariant);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setUnitPrice(itemDTO.getUnitPrice());
            orderItem.setNote(itemDTO.getNote());

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(itemDTO.getUnitPrice().multiply(new BigDecimal(itemDTO.getQuantity())));
        }

        orderItems.forEach(purchaseOrder::addPurchaseOrderItem);
        purchaseOrder.setTotalAmount(totalAmount);

        return purchaseOrderRepository.save(purchaseOrder);
    }

    public PurchaseOrder getPurchaseOrderById(Long orderId) {
        return purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Đơn Nhập Hàng với ID: " + orderId));
    }

    public Page<PurchaseOrder> listPurchaseOrders(PurchaseOrderStatusEnum statusFilter, Pageable pageable) {
        if (statusFilter != null) {
            return purchaseOrderRepository.findByStatus(statusFilter, pageable);
        }
        return purchaseOrderRepository.findAll(pageable);
    }

    @Transactional
    public PurchaseOrder receivePurchaseOrder(Long orderId) {
        PurchaseOrder purchaseOrder = getPurchaseOrderById(orderId);
        if (purchaseOrder.getStatus() != PurchaseOrderStatusEnum.PENDING) {
            throw new IllegalStateException(
                    "Chỉ có thể nhận hàng cho đơn hàng ở trạng thái PENDING. Trạng thái hiện tại: "
                            + purchaseOrder.getStatus());
        }

        purchaseOrder.setStatus(PurchaseOrderStatusEnum.RECEIVED);

        for (PurchaseOrderItem item : purchaseOrder.getPurchaseOrderItems()) {
            String reason = "Nhập hàng từ đơn PO#" + purchaseOrder.getId() + " (Item SKU: "
                    + item.getProductVariant().getSku() + ")";
            inventoryService.updateInventoryAndLogAudit(item.getProductVariant(), item.getQuantity(), reason,
                    purchaseOrder);
        }

        return purchaseOrderRepository.save(purchaseOrder);
    }

    @Transactional
    public PurchaseOrder cancelPurchaseOrder(Long orderId) {
        PurchaseOrder purchaseOrder = getPurchaseOrderById(orderId);

        if (purchaseOrder.getStatus() == PurchaseOrderStatusEnum.RECEIVED) {
            throw new IllegalStateException(
                    "Không thể hủy đơn nhập hàng đã nhận. Trạng thái hiện tại: " + purchaseOrder.getStatus());
        }
        if (purchaseOrder.getStatus() == PurchaseOrderStatusEnum.CANCELLED) {
            throw new IllegalStateException("Đơn nhập hàng này đã được hủy trước đó.");
        }

        purchaseOrder.setStatus(PurchaseOrderStatusEnum.CANCELLED);

        return purchaseOrderRepository.save(purchaseOrder);
    }

}
