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

/**
 * RestController này xử lý các yêu cầu API liên quan đến việc tạo đơn hàng.
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderService orderService;

    /**
     * API để xử lý yêu cầu đặt hàng từ form thanh toán của người dùng.
     * Endpoint này nhận dữ liệu thông tin giao hàng và tạo một đơn hàng mới.
     *
     * @param request DTO chứa thông tin giao hàng đã được validate.
     * @return ResponseEntity chứa ID của đơn hàng đã được tạo thành công.
     */
    @PostMapping
    public ResponseEntity<?> placeOrder(@Valid @RequestBody OrderCheckoutRequest request) {
        // Gọi đến OrderService để thực hiện logic tạo đơn hàng.
        // OrderService đã được thiết kế để xử lý các trường hợp nghiệp vụ phức tạp
        // như kiểm tra giỏ hàng, kiểm tra tồn kho và trừ kho.
        // Nếu có lỗi (ví dụ: hết hàng), OrderService sẽ ném ra một exception
        // và GlobalExceptionHandler sẽ bắt và trả về lỗi HTTP 400 cho client.
        Order savedOrder = orderService.createOrderFromCart(request);

        // Nếu thành công, trả về một đối tượng JSON đơn giản chứa ID của đơn hàng mới.
        // Ví dụ: { "orderId": 123 }
        // JavaScript ở frontend sẽ dùng ID này để điều hướng tới trang đặt hàng thành công.
        return ResponseEntity.ok(Map.of("orderId", savedOrder.getId()));
    }
}