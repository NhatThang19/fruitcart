package com.vn.fruitcart.controller.api;

import com.vn.fruitcart.entity.Order;
import com.vn.fruitcart.entity.dto.request.OrderCheckoutRequest;
import com.vn.fruitcart.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<?> placeOrder(@Valid @RequestBody OrderCheckoutRequest request) {

        Order savedOrder = orderService.createOrderFromCart(request);

        return ResponseEntity.ok(Map.of("orderId", savedOrder.getId()));
    }
}