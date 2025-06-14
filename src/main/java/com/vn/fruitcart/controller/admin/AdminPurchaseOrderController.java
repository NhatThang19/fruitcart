package com.vn.fruitcart.controller.admin;

import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Sort;

import com.vn.fruitcart.entity.ProductVariant;
import com.vn.fruitcart.entity.PurchaseOrder;
import com.vn.fruitcart.entity.dto.request.PurchaseOrderCreateReq;
import com.vn.fruitcart.exception.ResourceNotFoundException;
import com.vn.fruitcart.repository.ProductVariantRepository;
import com.vn.fruitcart.service.BreadcrumbService;
import com.vn.fruitcart.service.PurchaseOrderService;
import com.vn.fruitcart.util.FruitCartUtils;
import com.vn.fruitcart.util.constant.PurchaseOrderStatusEnum;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/purchase-orders")
@RequiredArgsConstructor
public class AdminPurchaseOrderController {
    private final PurchaseOrderService purchaseOrderService;
    private final ProductVariantRepository productVariantRepository;
    private final BreadcrumbService breadcrumbService;

    @GetMapping
    public String listPurchaseOrders(Model model,
            @PageableDefault(size = 5, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(name = "status", required = false) String statusFilterString) {

        model.addAttribute("pageMetadata", breadcrumbService.buildAdminPurchaseOrder());

        model.addAttribute("statusEnumValues", Arrays.asList(PurchaseOrderStatusEnum.values()));

        PurchaseOrderStatusEnum statusFilter = null;
        if (statusFilterString != null && !statusFilterString.isEmpty()
                && !"ALL".equalsIgnoreCase(statusFilterString)) {
            try {
                statusFilter = PurchaseOrderStatusEnum.valueOf(statusFilterString.toUpperCase());
            } catch (IllegalArgumentException e) {
                model.addAttribute("filterErrorMessage", "Giá trị trạng thái lọc không hợp lệ: " + statusFilterString);
            }
        }

        Page<PurchaseOrder> purchaseOrderPage = purchaseOrderService.listPurchaseOrders(statusFilter, pageable);

        model.addAttribute("purchaseOrdersPage", purchaseOrderPage);

        model.addAttribute("currentStatusFilter",
                statusFilterString != null ? statusFilterString.toUpperCase() : "ALL");

        FruitCartUtils.addPagingAndSortingAttributes(model, pageable);

        return "admin/pages/purchase_order/list";
    }

    @GetMapping("/new")
    public String showCreatePurchaseOrderForm(Model model) {
        if (!model.containsAttribute("purchaseOrderCreateRequestDTO")) {
            model.addAttribute("purchaseOrderCreateRequestDTO", new PurchaseOrderCreateReq());
        }
        List<ProductVariant> productVariants = productVariantRepository.findAll();
        model.addAttribute("productVariants", productVariants);
        model.addAttribute("pageMetadata", breadcrumbService.buildAdminPurchaseOrderCreate());

        return "admin/pages/purchase_order/create";
    }

    @PostMapping("/create")
    public String createPurchaseOrder(
            @Valid @ModelAttribute("purchaseOrderCreateRequestDTO") PurchaseOrderCreateReq createDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            List<ProductVariant> productVariants = productVariantRepository.findAll();
            model.addAttribute("productVariants", productVariants);
            model.addAttribute("pageMetadata", breadcrumbService.buildAdminOriginDetailPageMetadata());
            model.addAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin nhập liệu.");
            return "admin/pages/purchase_order/create";
        }
        try {
            PurchaseOrder createdOrder = purchaseOrderService.createPurchaseOrder(createDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Tạo đơn nhập hàng #" + createdOrder.getId() + " thành công!");
            return "redirect:/admin/purchase-orders";
        } catch (ResourceNotFoundException e) {
            List<ProductVariant> productVariants = productVariantRepository.findAll();
            model.addAttribute("productVariants", productVariants);
            model.addAttribute("pageMetadata", breadcrumbService.buildAdminOriginDetailPageMetadata());
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/pages/purchase_order/create";
        } catch (Exception e) {
            List<ProductVariant> productVariants = productVariantRepository.findAll();
            model.addAttribute("productVariants", productVariants);
            model.addAttribute("pageMetadata", breadcrumbService.buildAdminOriginDetailPageMetadata());
            model.addAttribute("errorMessage", "Đã có lỗi xảy ra trong quá trình tạo đơn nhập hàng.");
            return "admin/pages/purchase_order/create";
        }
    }

    @GetMapping("/{orderId}")
    public String viewPurchaseOrder(@PathVariable Long orderId, Model model, RedirectAttributes redirectAttributes) {
        try {
            PurchaseOrder purchaseOrder = purchaseOrderService.getPurchaseOrderById(orderId);
            model.addAttribute("purchaseOrder", purchaseOrder);
            model.addAttribute("pageMetadata", breadcrumbService.buildAdminPurchaseOrderDetail());
            model.addAttribute("statusEnum", PurchaseOrderStatusEnum.class);
            return "admin/pages/purchase_order/detail";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/purchase-orders";
        }
    }

    @PostMapping("/{orderId}/receive")
    public String receivePurchaseOrder(@PathVariable Long orderId, RedirectAttributes redirectAttributes) {
        try {
            PurchaseOrder updatedOrder = purchaseOrderService.receivePurchaseOrder(orderId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã xác nhận nhận hàng cho đơn #" + updatedOrder.getId() + ".");
        } catch (ResourceNotFoundException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Lỗi không xác định khi nhận hàng cho đơn #" + orderId + ".");
        }
        return "redirect:/admin/purchase-orders/" + orderId;
    }

    @PostMapping("/{orderId}/cancel")
    public String cancelPurchaseOrder(@PathVariable Long orderId, RedirectAttributes redirectAttributes) {
        try {
            PurchaseOrder updatedOrder = purchaseOrderService.cancelPurchaseOrder(orderId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã hủy đơn nhập hàng #" + updatedOrder.getId() + ".");
        } catch (ResourceNotFoundException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Lỗi không xác định khi hủy đơn hàng #" + orderId + ".");
        }
        return "redirect:/admin/purchase-orders/" + orderId;
    }
}
