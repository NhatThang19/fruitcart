package com.vn.fruitcart.controller.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.vn.fruitcart.entity.Order;
import com.vn.fruitcart.entity.dto.request.OrderSearchCriteria;
import com.vn.fruitcart.service.BreadcrumbService;
import com.vn.fruitcart.service.OrderService;
import com.vn.fruitcart.util.FruitCartUtils;
import com.vn.fruitcart.util.constant.OrderStatusEnum;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {
    private final OrderService orderService;
    private final BreadcrumbService breadcrumbService;

    @GetMapping
    public String listOrigins(Model model,
            @PageableDefault(size = 5, sort = "id", direction = Direction.DESC) Pageable pageable,
            @ModelAttribute("criteria") OrderSearchCriteria criteria) {
        model.addAttribute("pageMetadata", breadcrumbService.buildAdminOrder());

        Page<Order> ordersPage = orderService.findOrdersByCriteria(criteria, pageable);

        model.addAttribute("ordersPage", ordersPage);

        model.addAttribute("orderStatuses", OrderStatusEnum.values());

        FruitCartUtils.addPagingAndSortingAttributes(model, pageable);

        return "admin/pages/order/list";
    }
}
