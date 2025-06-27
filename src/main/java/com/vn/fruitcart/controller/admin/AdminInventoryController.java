package com.vn.fruitcart.controller.admin;

import com.vn.fruitcart.entity.InventoryAudit;
import com.vn.fruitcart.entity.ProductVariant;
import com.vn.fruitcart.entity.dto.request.inventory.InventoryAuditSearchCriteriaReq;
import com.vn.fruitcart.entity.dto.request.inventory.StocktakeItemReq;
import com.vn.fruitcart.entity.dto.request.inventory.StocktakeReq;
import com.vn.fruitcart.exception.ResourceNotFoundException;
import com.vn.fruitcart.repository.ProductVariantRepository;
import com.vn.fruitcart.service.BreadcrumbService;
import com.vn.fruitcart.service.InventoryService;
import com.vn.fruitcart.util.FruitCartUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/inventory")
@RequiredArgsConstructor
public class AdminInventoryController {

    private final InventoryService inventoryService;
    private final ProductVariantRepository productVariantRepository;
    private final BreadcrumbService breadcrumbService;

    @GetMapping("/stocktake/form")
    public String showStocktakeForm(Model model) {
        if (!model.containsAttribute("stocktakeRequestDTO")) {
            StocktakeReq requestDTO = new StocktakeReq();
            requestDTO.getItems().add(new StocktakeItemReq());
            model.addAttribute("stocktakeRequestDTO", requestDTO);
        }

        List<ProductVariant> productVariants = productVariantRepository.findAll();
        model.addAttribute("productVariants", productVariants);
        model.addAttribute("pageMetadata", breadcrumbService.buildAdminStocktakePageMetadata());

        return "admin/pages/inventory/stocktake_form";
    }

    @PostMapping("/stocktake/perform")
    public String performStocktake(
            @Valid @ModelAttribute("stocktakeRequestDTO") StocktakeReq stocktakeRequestDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (stocktakeRequestDTO.getItems() != null) {
            long distinctCount = stocktakeRequestDTO.getItems().stream()
                    .map(StocktakeItemReq::getProductVariantId)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .count();
            if (distinctCount < stocktakeRequestDTO.getItems().stream().filter(i -> i.getProductVariantId() != null).count()) {
                bindingResult.reject("", "Không thể kiểm kê cùng một sản phẩm nhiều lần trong một phiếu.");
            }
        }

        if (bindingResult.hasErrors()) {
            populateStocktakeFormModel(model, "Vui lòng kiểm tra lại thông tin nhập liệu.");
            model.addAttribute("stocktakeRequestDTO", stocktakeRequestDTO);
            return "admin/pages/inventory/stocktake_form";
        }

        try {
            inventoryService.performMultiProductStocktake(stocktakeRequestDTO);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã thực hiện kiểm kê thành công cho " + stocktakeRequestDTO.getItems().size() + " loại sản phẩm.");
            return "redirect:/admin/inventory/stocktake/form";
        } catch (ResourceNotFoundException | IllegalArgumentException | IllegalStateException e) {
            populateStocktakeFormModel(model, e.getMessage());
            model.addAttribute("stocktakeRequestDTO", stocktakeRequestDTO);
            return "admin/pages/inventory/stocktake_form";
        } catch (Exception e) {
            populateStocktakeFormModel(model, "Đã có lỗi không mong muốn xảy ra: " + e.getMessage());
            model.addAttribute("stocktakeRequestDTO", stocktakeRequestDTO);
            return "admin/pages/inventory/stocktake_form";
        }
    }

    private void populateStocktakeFormModel(Model model, String errorMessage) {
        List<ProductVariant> productVariants = productVariantRepository.findAll();
        model.addAttribute("productVariants", productVariants);
        model.addAttribute("pageMetadata", breadcrumbService.buildAdminStocktakePageMetadata());
        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
        }
    }

    @GetMapping("/audits")
    public String listInventoryAudits(Model model,
                                      @PageableDefault(size = 15, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable,
                                      @ModelAttribute("criteria") InventoryAuditSearchCriteriaReq criteria) {

        Page<InventoryAudit> inventoryAuditsPage = inventoryService.listInventoryAudits(criteria, pageable);
        model.addAttribute("inventoryAuditsPage", inventoryAuditsPage);

        model.addAttribute("productVariants", productVariantRepository.findAll());

        FruitCartUtils.addPagingAndSortingAttributes(model, pageable);

        model.addAttribute("pageMetadata", breadcrumbService.buildAdminInventory());

        return "admin/pages/inventory/audit_list";
    }

    @GetMapping("/audits/{auditId}")
    public String viewInventoryAuditDetail(@PathVariable Long auditId, Model model, RedirectAttributes redirectAttributes) {
        try {
            InventoryAudit inventoryAudit = inventoryService.findAuditById(auditId);
            model.addAttribute("inventoryAudit", inventoryAudit);
            model.addAttribute("pageMetadata", breadcrumbService.buildAdminInventoryAuditDetail());
            return "admin/pages/inventory/audit_detail";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/inventory/audits";
        }
    }
}