package com.vn.fruitcart.controller.admin;

import com.vn.fruitcart.entity.Discount;
import com.vn.fruitcart.entity.ProductVariant;
import com.vn.fruitcart.entity.dto.request.DiscountReq;
import com.vn.fruitcart.entity.dto.request.DiscountSearchCriteriaReq;
import com.vn.fruitcart.service.BreadcrumbService;
import com.vn.fruitcart.service.DiscountService;
import com.vn.fruitcart.util.FruitCartUtils;
import com.vn.fruitcart.util.constant.CustomerClusterEnum;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/discounts")
@RequiredArgsConstructor
public class AdminDiscountController {
    private final DiscountService discountService;
    private final BreadcrumbService breadcrumbService;

    @GetMapping
    public String listDiscounts(Model model,
                                @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                @ModelAttribute("criteria") DiscountSearchCriteriaReq criteria) {

        model.addAttribute("pageMetadata", breadcrumbService.buildAdminDiscountList());

        Page<Discount> discountsPage = discountService.findByCriteria(criteria, pageable);

        model.addAttribute("discountsPage", discountsPage);

        FruitCartUtils.addPagingAndSortingAttributes(model, pageable);

        return "admin/pages/discount/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("pageMetadata", breadcrumbService.buildAdminDiscount());
        model.addAttribute("discount", new Discount());
        model.addAttribute("customerClusters", CustomerClusterEnum.values());
        model.addAttribute("allVariants", discountService.findAllVariants());
        return "admin/pages/discount/form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("pageMetadata", breadcrumbService.buildAdminDiscount());
        Discount discount = discountService.findById(id);
        model.addAttribute("customerClusters", CustomerClusterEnum.values());

        List<Long> selectedVariantIds = discount.getVariants().stream()
                .map(ProductVariant::getId)
                .collect(Collectors.toList());

        model.addAttribute("discount", discount);
        model.addAttribute("allVariants", discountService.findAllVariants());
        model.addAttribute("selectedVariantIds", selectedVariantIds);
        return "admin/pages/discount/form";
    }

    @PostMapping("/save")
    public String saveDiscount(@Valid @ModelAttribute("discount") DiscountReq discountReq,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes ra) {
        model.addAttribute("pageMetadata", breadcrumbService.buildAdminDiscount());
        if (bindingResult.hasErrors()) {
            model.addAttribute("allVariants", discountService.findAllVariants());
            model.addAttribute("selectedVariantIds", discountReq.getVariantIds());
            model.addAttribute("pageTitle", discountReq.getId() == null ? "Tạo mới Giảm giá (có lỗi)" : "Cập nhật Giảm giá (có lỗi)");

            return "admin/pages/discount/form";
        }

        try {
            discountService.saveFromRequest(discountReq);
            ra.addFlashAttribute("successMessage", "Đã lưu chương trình giảm giá thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi khi lưu chương trình giảm giá: " + e.getMessage());
        }
        return "redirect:/admin/discounts";
    }

    @PostMapping("/delete/{id}")
    public String deleteDiscount(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            discountService.deleteById(id);
            ra.addFlashAttribute("successMessage", "Đã xóa chương trình giảm giá thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Không thể xóa chương trình giảm giá này.");
        }
        return "redirect:/admin/discounts";
    }
}
