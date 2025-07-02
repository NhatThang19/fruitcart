package com.vn.fruitcart.controller.admin;

import com.vn.fruitcart.entity.Order;
import com.vn.fruitcart.entity.dto.request.OrderSearchCriteria;
import com.vn.fruitcart.exception.ResourceNotFoundException;
import com.vn.fruitcart.service.BreadcrumbService;
import com.vn.fruitcart.service.OrderService;
import com.vn.fruitcart.util.FruitCartUtils;
import com.vn.fruitcart.util.constant.EOrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;
     private final BreadcrumbService breadcrumbService;

    @GetMapping
    public String listOrders(Model model,
                             @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable,
                             @ModelAttribute("criteria") OrderSearchCriteria criteria) {

         model.addAttribute("pageMetadata", breadcrumbService.buildAdminOrder());

        Page<Order> orderPage = orderService.findByCriteria(criteria, pageable);

        model.addAttribute("orderPage", orderPage);
        model.addAttribute("statuses", EOrderStatus.values());

        FruitCartUtils.addPagingAndSortingAttributes(model, pageable);

        return "admin/pages/order/list";
    }

    @GetMapping("/{id}")
    public String detailOrder(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        model.addAttribute("pageMetadata", breadcrumbService.buildAdminOrderDetail());
        return orderService.findById(id)
                .map(order -> {
                    model.addAttribute("order", order);
                    return "admin/pages/order/detail";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng với ID: " + id);
                    return "redirect:/admin/orders";
                });
    }

    @PostMapping("/{id}/update-status")
    public String updateStatus(@PathVariable("id") Long id,
                               @RequestParam("status") EOrderStatus newStatus,
                               RedirectAttributes redirectAttributes) {
        try {
            orderService.updateStatus(id, newStatus);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái đơn hàng #" + id + " thành công!");
        } catch (ResourceNotFoundException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã có lỗi không mong muốn xảy ra.");
        }

        return "redirect:/admin/orders/" + id;
    }
}