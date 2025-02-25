package com.sellect.server.payment.controller;

import com.sellect.server.auth.domain.User;
import com.sellect.server.common.infrastructure.annotation.AuthUser;
import com.sellect.server.common.response.ApiResponse;
import com.sellect.server.payment.application.PaymentService;
import com.sellect.server.payment.controller.request.PaymentRequest;
import com.sellect.server.payment.controller.response.PaymentHistoryResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/history")
    public ApiResponse<List<PaymentHistoryResponse>> getPaymentHistory(
        @AuthUser User user,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "5") int size) {
        List<PaymentHistoryResponse> paymentHistory = paymentService.getPaymentHistory(user, page,
            size);
        return ApiResponse.ok(paymentHistory);
    }

    @PostMapping("/ready")
    public ApiResponse<String> initialPayment(@AuthUser User user,
        @RequestBody PaymentRequest request) {
        String redirectUrl = paymentService.initialPayment(user, request);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", redirectUrl);
        log.info(redirectUrl);
        return ApiResponse.ok(redirectUrl);
    }

    @GetMapping("/success/{pid}")
    public String approvePayment(
        @PathVariable String pid,
        @RequestParam("pg_token") String token) {
        paymentService.approvePayment(pid, token);
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

    @GetMapping("/cancel/{pid}")
    public ApiResponse<Object> cancelPayment(
        @PathVariable String pid
    ) {
        paymentService.cancelPayment(pid);
        return ApiResponse.ok();
    }

    @GetMapping("/fail/{pid}")
    public ApiResponse<Object> failPayment(
        @PathVariable String pid
    ) {
        paymentService.failPayment(pid);
        return ApiResponse.ok();
    }
}
