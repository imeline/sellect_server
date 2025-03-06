package com.sellect.server.payment.controller;

import com.sellect.server.auth.domain.User;
import com.sellect.server.common.infrastructure.annotation.AuthUser;
import com.sellect.server.common.response.ApiResponse;
import com.sellect.server.payment.application.PaymentService;
import com.sellect.server.payment.controller.response.PaymentHistoryResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
        @PageableDefault(page = 0, size = 5, sort = "createdAt", direction = Direction.DESC) Pageable pageable) {
        List<PaymentHistoryResponse> paymentHistory = paymentService.getPaymentHistory(user,
            pageable);
        return ApiResponse.ok(paymentHistory);
    }


}
