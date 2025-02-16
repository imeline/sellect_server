package com.sellect.server.order.controller;

import com.sellect.server.auth.domain.User;
import com.sellect.server.common.infrastructure.annotation.AuthSeller;
import com.sellect.server.common.response.ApiResponse;
import com.sellect.server.order.application.OrderService;
import com.sellect.server.order.controller.response.OrderDetailGetResponse;
import com.sellect.server.order.controller.response.OrderGetResponse;
import com.sellect.server.product.controller.request.ProductRegisterRequest;
import com.sellect.server.product.controller.response.ProductRegisterResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1")
@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문서 생성(결제 전)
     */
    @PostMapping("/order/pending")
    public ApiResponse<ProductRegisterResponse> registerMultiple(@AuthSeller User user,
        @Valid @RequestBody List<ProductRegisterRequest> requests) {

        long orderId = productService.registerMultiple(seller, requests);
        return ApiResponse.ok(result);
    }

    /**
     * 주문 완료
     */
    @PatchMapping("/order/complete")
    public ApiResponse<ProductRegisterResponse> registerMultiple(@AuthSeller User user,
        @Valid @RequestBody List<ProductRegisterRequest> requests) {

        long orderId = productService.registerMultiple(seller, requests);
        return ApiResponse.ok(result);
    }

    /**
     * 주문 내역 확인
     */
    @GetMapping("/orders")
    public ApiResponse<List<OrderGetResponse>> getOrdersByUser(@AuthSeller User user) {
        return ApiResponse.ok(orderService.getOrdersByUser(user));
    }

    /**
     * 주문 내역 상세 확인
     */
    @GetMapping("/orders/{orderId}")
    public ApiResponse<OrderDetailGetResponse> getOrdersByUser(@AuthSeller User user,
        @PathVariable Long orderId) {
        return ApiResponse.ok(orderService.getOrderDetail(user, orderId));
    }
}
