package com.sellect.server.order.controller;

import com.sellect.server.auth.domain.User;
import com.sellect.server.common.infrastructure.annotation.AuthUser;
import com.sellect.server.common.response.ApiResponse;
import com.sellect.server.order.application.OrderService;
import com.sellect.server.order.controller.request.OrderAddRequest;
import com.sellect.server.order.controller.response.OrderDetailGetResponse;
import com.sellect.server.order.controller.response.OrderGetResponse;
import com.sellect.server.order.controller.response.OrderItemPendingReadResponse;
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
     * 주문 페이지 조회용 (결제 전)
     */
    @GetMapping("/orders/{orderId}/pending")
    public ApiResponse<List<OrderItemPendingReadResponse>> readPending(
        @AuthUser User user,
        @PathVariable Long orderId
    ) {
        List<OrderItemPendingReadResponse> result = orderService.readPending(
            user, orderId);

        return ApiResponse.ok(result);
    }

    /**
     * 주문 생성(pending)
     */
    @PostMapping("/order/pending")
    public ApiResponse<Long> registerPendingOrder(@AuthUser User user,
        @Valid @RequestBody OrderAddRequest requests) {

        Long orderId = orderService.registerPendingOrder(user, requests).getId();
        return ApiResponse.ok(orderId);
    }

//    /**
//     * 결제 전 - 재고 확인, DB 락
//     */
//    @PatchMapping("/order/{orderId}/reserve/stock")
//    public ApiResponse<Void> reserveStock(
//        @PathVariable Long orderId) {
//
//        orderService.reserveStock(orderId);
//        return ApiResponse.ok(null);
//    }

    /**
     * 주문 완료 - 주문 상태 확정, 쿠폰 삭제, 장바구니 비우기
     */
    @PostMapping("/order/complete/{orderId}")
    public ApiResponse<Void> completeOrder(@AuthUser User user, @PathVariable Long orderId) {
        orderService.completeOrder(user, orderId);
        return ApiResponse.ok(null);
    }


    /**
     * 주문 내역 확인
     */
    @GetMapping("/orders")
    public ApiResponse<List<OrderGetResponse>> getOrdersByUser(@AuthUser User user) {
        return ApiResponse.ok(orderService.getOrdersByUser(user));
    }

    /**
     * 주문 내역 상세 확인
     */
    @GetMapping("/orders/{orderId}")
    public ApiResponse<OrderDetailGetResponse> getOrdersByUser(@PathVariable Long orderId) {
        return ApiResponse.ok(orderService.getOrderDetail(orderId));
    }

    @PatchMapping("/order/{orderId}/appied-coupon/{couponId}")
    public ApiResponse<Void> applyCoupon(@AuthUser User user, @PathVariable Long orderId,
        @PathVariable Long couponId) {
        orderService.applyCouponToOrder(user, orderId, couponId);
        return ApiResponse.ok(null);
    }
}
