package com.vn.fruitcart.controller.admin;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.vn.fruitcart.entity.InventoryAudit;
import com.vn.fruitcart.entity.ProductVariant;
import com.vn.fruitcart.exception.ResourceNotFoundException;
import com.vn.fruitcart.repository.ProductVariantRepository;
import com.vn.fruitcart.service.BreadcrumbService;
import com.vn.fruitcart.service.InventoryAuditService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/inventory/audits")
@RequiredArgsConstructor
public class InventoryAuditController {
    private final InventoryAuditService inventoryAuditService;
    private final ProductVariantRepository productVariantRepository;
    private final BreadcrumbService breadcrumbService;

    @GetMapping
    public String listInventoryAudits(Model model,
            @PageableDefault(size = 15, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(name = "productVariantId", required = false) Long productVariantIdFilter,
            @RequestParam(name = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFromFilter,
            @RequestParam(name = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateToFilter) {

        Page<InventoryAudit> inventoryAuditsPage = inventoryAuditService.listInventoryAudits(
                productVariantIdFilter,
                dateFromFilter,
                dateToFilter,
                pageable);

        List<ProductVariant> productVariants = productVariantRepository.findAll();

        model.addAttribute("inventoryAuditsPage", inventoryAuditsPage);
        model.addAttribute("productVariants", productVariants);

        model.addAttribute("currentProductVariantIdFilter", productVariantIdFilter);
        model.addAttribute("currentDateFromFilter", dateFromFilter);
        model.addAttribute("currentDateToFilter", dateToFilter);

        String currentSortField = "";
        String currentSortDirection = "";
        if (pageable.getSort().isSorted()) {
            Sort.Order order = pageable.getSort().iterator().next();
            currentSortField = order.getProperty();
            currentSortDirection = order.getDirection().name();
        }
        model.addAttribute("currentSortField", currentSortField);
        model.addAttribute("currentSortDirection", currentSortDirection);
        model.addAttribute("currentSortParam", pageable.getSort().get()
                .map(order -> order.getProperty() + "," + order.getDirection().name().toLowerCase())
                .collect(Collectors.joining()));

        model.addAttribute("pageMetadata", breadcrumbService.buildAdminOriginDetailPageMetadata());
        return "admin/pages/inventory/audit_list";
    }

    @GetMapping("/{auditId}")
    public String viewInventoryAuditDetail(@PathVariable Long auditId, Model model,
            RedirectAttributes redirectAttributes) {
        try {
            InventoryAudit inventoryAudit = inventoryAuditService.findById(auditId);

            model.addAttribute("inventoryAudit", inventoryAudit);
            model.addAttribute("pageMetadata", breadcrumbService.buildAdminOriginDetailPageMetadata());
            return "admin/pages/inventory/audit_detail";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/inventory/audits";
        }
    }
}
