package com.sellect.server.payment.application;

import com.sellect.server.auth.domain.User;
import com.sellect.server.common.exception.CommonException;
import com.sellect.server.common.exception.enums.BError;
import com.sellect.server.order.Infrastructure.port.KakaoPayClient;
import com.sellect.server.order.Infrastructure.request.KakaoPayReadyRequest;
import com.sellect.server.order.Infrastructure.response.KakaoPayApproveResponse;
import com.sellect.server.order.Infrastructure.response.KakaoPayReadyResponse;
import com.sellect.server.order.domain.Orders;
import com.sellect.server.payment.controller.request.ApproveRequest;
import com.sellect.server.payment.controller.response.PaymentHistoryResponse;
import com.sellect.server.payment.domain.Payment;
import com.sellect.server.payment.repository.PaymentRepository;
import java.util.List;
import java.util.UUID;
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
    private final KakaoPayClient kakaoPayClient;

    // 기본 최신순서
    // 최근 결제 내역 조회
    // 상위 5개 조회
    @Transactional(readOnly = true)
    public List<PaymentHistoryResponse> getPaymentHistory(User user, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Direction.DESC, "createdAt");
        Page<Payment> paymentHistoryByUser = paymentRepository.findPaymentHistoryByUser(
            user.getUuid(), pageable);

        return paymentHistoryByUser.getContent().stream()
            .map(PaymentHistoryResponse::of)
            .toList();
    }

    public void readyPayment(User user, Long orderId, String pid, Orders order, String tid) {
        Payment payment = Payment.ready(String.valueOf(orderId),
            pid,
            user.getUuid(),
            order.getTotalPrice().intValue(),
            tid);

        paymentRepository.save(payment);
    }

    public String getKakaoPayReadyResponse(User user, Long orderId, Orders order) {
        String pid = generatePaymentId();
        Integer quantity = 0;
        KakaoPayReadyRequest request = kakaoPayClient.createKakaoPayReadyRequest(
            String.valueOf(orderId),
            user.getUuid(),
            "test",
            quantity,
            order.getTotalPrice().intValue(),
            pid
        );
        KakaoPayReadyResponse kakaoPayReadyResponse = kakaoPayClient.readyPayment(request);
        readyPayment(user, orderId, pid, order, kakaoPayReadyResponse.tid());
        return kakaoPayReadyResponse.next_redirect_pc_url();
    }

    private String generatePaymentId() {
        return String.valueOf(UUID.randomUUID());
    }

    public Payment findReadyPaymentByPid(String pid) {
        return paymentRepository.findByPid(pid)
            .orElseThrow(() -> new CommonException(BError.NOT_EXIST, String.format("Payment %s", pid)));
    }

    public void paymentApprove(String pid, String token, Payment payment) {
        Payment approvePayment = payment.approvePayment();
        paymentRepository.save(approvePayment);

        // 카카오 한테 요청 보내기
        ApproveRequest approveRequest = ApproveRequest.builder()
            .cid("TC0ONETIME")
            .tid(payment.getTid())
            .partnerOrderId(payment.getOrderId())
            .partnerUserId(payment.getUid())
            .pgToken(token)
            .build();

        KakaoPayApproveResponse kakaoPayApproveResponse = kakaoPayClient.paymentApprove(approveRequest);
        log.info("Payment approved for pid: {}", pid);
    }
}

