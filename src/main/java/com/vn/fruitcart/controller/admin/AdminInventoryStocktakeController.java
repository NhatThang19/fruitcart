package com.vn.fruitcart.controller.admin;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vn.fruitcart.entity.ProductVariant;
import com.vn.fruitcart.entity.dto.request.StocktakeReq;
import com.vn.fruitcart.exception.ResourceNotFoundException;
import com.vn.fruitcart.repository.ProductVariantRepository;
import com.vn.fruitcart.service.BreadcrumbService;
import com.vn.fruitcart.service.InventoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/inventory/stocktake")
@RequiredArgsConstructor
public class AdminInventoryStocktakeController {
    private final InventoryService inventoryService;
    private final ProductVariantRepository productVariantRepository;
    private final BreadcrumbService breadcrumbService;

    @GetMapping("/form")
    public String showStocktakeForm(Model model) {
        if (!model.containsAttribute("stocktakeRequestDTO")) {
            model.addAttribute("stocktakeRequestDTO", new StocktakeReq());
        }

        List<ProductVariant> productVariants = productVariantRepository.findAll();
        model.addAttribute("productVariants", productVariants);

        model.addAttribute("pageMetadata", breadcrumbService.buildAdminOriginDetailPageMetadata());

        return "admin/pages/inventory/stocktake_form";
    }

    @PostMapping("/perform")
    public String performStocktake(
            @Valid @ModelAttribute("stocktakeRequestDTO") StocktakeReq stocktakeRequestDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            List<ProductVariant> productVariants = productVariantRepository.findAll();
            model.addAttribute("productVariants", productVariants);
            model.addAttribute("pageMetadata", breadcrumbService.buildAdminOriginDetailPageMetadata());
            model.addAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin nhập liệu.");
            return "admin/pages/inventory/stocktake_form";
        }

        try {
            inventoryService.performStocktake(
                    stocktakeRequestDTO.getProductVariantId(),
                    stocktakeRequestDTO.getActualCountedQuantity(),
                    stocktakeRequestDTO.getStocktakeNote());
            String productName = productVariantRepository.findById(stocktakeRequestDTO.getProductVariantId())
                    .map(pv -> pv.getProduct().getName() + " - " + pv.getAttribute() + " (SKU: " + pv.getSku() + ")")
                    .orElse("Sản phẩm ID " + stocktakeRequestDTO.getProductVariantId());

            redirectAttributes.addFlashAttribute("successMessage", "Đã thực hiện kiểm kê thành công cho: " + productName
                    + ". Số lượng mới: " + stocktakeRequestDTO.getActualCountedQuantity());
            return "redirect:/admin/inventory/stocktake/form";
        } catch (ResourceNotFoundException | IllegalArgumentException e) { //
            List<ProductVariant> productVariants = productVariantRepository.findAll();
            model.addAttribute("productVariants", productVariants);
            model.addAttribute("pageMetadata", breadcrumbService.buildAdminOriginDetailPageMetadata());
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/pages/inventory/stocktake_form";
        } catch (Exception e) {
            List<ProductVariant> productVariants = productVariantRepository.findAll();
            model.addAttribute("productVariants", productVariants);
            model.addAttribute("errorMessage", "Đã có lỗi không mong muốn xảy ra trong quá trình thực hiện kiểm kê.");
            return "admin/pages/inventory/stocktake_form";
        }
    }
}
