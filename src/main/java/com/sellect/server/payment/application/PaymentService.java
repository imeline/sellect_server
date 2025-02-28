package com.sellect.server.payment.application;

import com.sellect.server.auth.domain.User;
import com.sellect.server.payment.controller.response.PaymentHistoryResponse;
import com.sellect.server.payment.domain.Payment;
import com.sellect.server.payment.repository.PaymentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    // 기본 최신순서
    // 최근 결제 내역 조회
    // 상위 5개 조회
    @Transactional(readOnly = true)
    public List<PaymentHistoryResponse> getPaymentHistory(User user, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Direction.DESC, "createdAt");
        Page<Payment> paymentHistoryByUser = paymentRepository.findPaymentHistoryByUser(
            user.getUuid(), pageable);

        return paymentHistoryByUser.getContent().stream()
            .map(payment ->
                PaymentHistoryResponse.builder()
                    .id(payment.getId())
                    .orderId(payment.getOrderId())
                    .pid(payment.getPid())
                    .status(String.valueOf(payment.getStatus()))
                    .price(String.valueOf(payment.getPrice()))
                    .createdAt(payment.getCreatedAt().toString())
                    .build()).toList();
    }
}

