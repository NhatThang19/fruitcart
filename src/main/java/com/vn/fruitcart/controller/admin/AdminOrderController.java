package com.vn.fruitcart.controller.admin;

import com.vn.fruitcart.entity.Order;
import com.vn.fruitcart.entity.dto.request.OrderSearchCriteria;
import com.vn.fruitcart.exception.ResourceNotFoundException;
import com.vn.fruitcart.service.BreadcrumbService;
import com.vn.fruitcart.service.OrderService;
import com.vn.fruitcart.util.FruitCartUtils; // Giả sử bạn có class tiện ích này
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
     private final BreadcrumbService breadcrumbService; // Giả sử bạn có

    /**
     * Hiển thị trang danh sách đơn hàng với phân trang và lọc.
     * Dữ liệu được tải trực tiếp từ server và truyền vào view.
     */
    @GetMapping
    public String listOrders(Model model,
                             @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable,
                             @ModelAttribute("criteria") OrderSearchCriteria criteria) {

         model.addAttribute("pageMetadata", breadcrumbService.demo()); // Tùy chọn

        // Gọi service để lấy dữ liệu đã được phân trang và lọc
        Page<Order> orderPage = orderService.findByCriteria(criteria, pageable);

        model.addAttribute("orderPage", orderPage);
        model.addAttribute("statuses", EOrderStatus.values()); // Để điền vào dropdown lọc

        // Thêm các thuộc tính phân trang và sắp xếp vào model
        FruitCartUtils.addPagingAndSortingAttributes(model, pageable);

        return "admin/pages/order/list";
    }

    /**
     * Hiển thị trang chi tiết đơn hàng.
     */
    @GetMapping("/{id}")
    public String detailOrder(@PathVariable("id") Long id, Model model, RedirectAttributes ra) {
        model.addAttribute("pageMetadata", breadcrumbService.demo());
        return orderService.findById(id)
                .map(order -> {
                    model.addAttribute("order", order);
                    // model.addAttribute("pageMetadata", ...);
                    return "admin/pages/order/detail";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng với ID: " + id);
                    return "redirect:/admin/orders";
                });
    }

    /**
     * Xử lý yêu cầu cập nhật trạng thái từ form.
     */
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

        // Quay trở lại trang chi tiết sau khi cập nhật
        return "redirect:/admin/orders/" + id;
    }
}