package com.sellect.server.order.controller;

import com.sellect.server.auth.domain.User;
import com.sellect.server.common.infrastructure.annotation.AuthUser;
import com.sellect.server.common.response.ApiResponse;
import com.sellect.server.order.application.OrderServiceV0;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v0")
public class OrderControllerV0 {
    private final OrderServiceV0 orderService;

    // 리팩터링 전(이벤트 기반 리팩터링 전)
    // 주문 생성(pending 상태) -> 결제 요청(카카오페이 api, redirect URL을 넘겨받음) -> redirectURL을
    @PostMapping("/order/payment/{orderId}/ready")
    public ApiResponse<String> readyPayment(@AuthUser User user, @PathVariable Long orderId,
        @RequestParam(name = "coupon_id", required = false) Long userReceivedCouponId) {

        log.info("[V0] ready!!");
        String redirectionUrl = orderService.payOrder(user, orderId, userReceivedCouponId);
        return ApiResponse.ok(redirectionUrl);
    }

    // 테스트를 위해서 approve를 위해 이곳에 api url 설정
    @GetMapping("/kakao-pay/success/{pid}")
    public String approvePayment(
        @PathVariable String pid,
        @RequestParam("pg_token") String token) {

        log.info("[V0] approve");

        orderService.approvePayment(pid, token);
        return """
            <!DOCTYPE html>
            <html lang="ko">
            <head>
                <meta charset="UTF-8">
                <title>결제 성공</title>
                <link rel="icon" href="data:,"> <!-- favicon 요청 방지 -->
                <style>
                    body { font-family: Arial, sans-serif; text-align: center; padding: 50px; }
                    h1 { color: #4CAF50; }
                    p { font-size: 18px; }
                    button { 
                        padding: 10px 20px; 
                        font-size: 16px; 
                        color: white; 
                        background-color: #007BFF; 
                        border: none; 
                        border-radius: 5px; 
                        cursor: pointer; 
                    }
                    button:hover { background-color: #0056b3; }
                </style>
            </head>
            <body>
                <h1>결제 성공!</h1>
                <p>결제가 정상적으로 완료되었습니다.</p>
                <button onclick="window.close()">확인</button>
            </body>
            </html>
            """;
    }

    @GetMapping("/kakao-pay/cancel/{pid}")
    public ApiResponse<Object> cancelPayment(
        @PathVariable String pid
    ) {
//        paymentService.cancelPayment(pid);
        return ApiResponse.ok();
    }

    @GetMapping("/kakao-pay/fail/{pid}")
    public ApiResponse<Object> failPayment(
        @PathVariable String pid
    ) {
//        paymentService.failPayment(pid);
        return ApiResponse.ok();
    }


}
