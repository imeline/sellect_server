package com.sellect.server.payment.application;

import com.sellect.server.auth.domain.User;
import com.sellect.server.auth.repository.user.UserRepository;
import com.sellect.server.common.exception.CommonException;
import com.sellect.server.common.exception.enums.BError;
import com.sellect.server.order.application.OrderService;
import com.sellect.server.payment.controller.request.ApproveRequest;
import com.sellect.server.payment.controller.request.KakaoPayReadyRequest;
import com.sellect.server.payment.controller.request.PaymentRequest;
import com.sellect.server.payment.controller.response.KakaoPayReadyResponse;
import com.sellect.server.payment.controller.response.PaymentHistoryResponse;
import com.sellect.server.payment.domain.Payment;
import com.sellect.server.payment.repository.PaymentRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    @Value("${kakao.pay.secret-key}")
    private String PAY_SECRET_KEY;
    public static final String TEST_API_CID = "TC0ONETIME";
    private static final String KAKAO_PAY_API_URL = "https://open-api.kakaopay.com/online/v1/payment/ready";
    private static final String KAKAO_PAY_APPROVE_API_URL = "https://open-api.kakaopay.com/online/v1/payment/approve";

    private final OrderService orderService;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    public String initialPayment(User user, PaymentRequest paymentRequest) {
        String pid = generatePaymentId();
        KakaoPayReadyRequest apiRequest = createKakaoPayReadyRequest(user, paymentRequest, pid);
        ResponseEntity<KakaoPayReadyResponse> response = sendKakaoPayReadyRequest(apiRequest);

        return handleKakaoPayReadyResponse(response, paymentRequest, user, pid);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void approvePayment(String pid, String token) {
        Payment payment = paymentRepository.findByPid(pid)
            .orElseThrow(
                () -> new CommonException(BError.NOT_EXIST, String.format("Payment %s", pid)));
        try {
            Long orderId = Long.valueOf(payment.getOrderId());
            orderService.lockProductItems(orderId);
            User user = userRepository.findByUuid(payment.getUid())
                .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "user"));
            orderService.completeOrder(user, orderId);

            ApproveRequest approveRequest = createApproveRequest(payment, token);
            handleKakaoPayApproveResponse(payment);
            ResponseEntity<Map> response = sendKakaoPayApproveRequest(approveRequest);
            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Failed to approve KakaoPay payment");
            }

            log.info("Payment approved for pid: {}", pid);
        } catch (Exception e) {
            log.error("Payment approval failed for pid: {}", pid, e);
            failPayment(pid); // 비동기 실패 처리
            throw new CommonException(BError.PAYMENT_FAILED, e.getMessage());
        }

        // todo
        // 결제 승인 전 (한 트랜잭션으로 묶는 단위)
        // 1. 재고차감
        // 2. 주문 확정
        // 3. 결제 확정
        // TODO: 실패시 카카오 페이 서버에게 결제 실패 요청 보내기 2025-02-20, 13:30

    }

    // 기본 최신순서
    // 최근 결제 내역 조회
    // 상위 5개 조회
    @Transactional(readOnly = true)
    public List<PaymentHistoryResponse> getPaymentHistory(User user, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Direction.DESC, "createdAt");
        Page<Payment> paymentHistoryByUser = paymentRepository.findPaymentHistoryByUser(
            user.getUuid(), pageable);

        List<PaymentHistoryResponse> responses = paymentHistoryByUser.getContent().stream()
            .map(payment ->
                PaymentHistoryResponse.builder()
                    .id(payment.getId())
                    .orderId(payment.getOrderId())
                    .pid(payment.getPid())
                    .status(String.valueOf(payment.getStatus()))
                    .price(String.valueOf(payment.getPrice()))
                    .createdAt(payment.getCreatedAt().toString())
                    .build()).toList();
        return responses;
    }

    @Async
    @Transactional
    public void cancelPayment(String pid) {
        Payment payment = paymentRepository.findByPid(pid).orElseThrow(
            () -> new CommonException(BError.NOT_EXIST, String.format("Payment %s", pid)));
        Payment cancelledPayment = payment.cancelPayment();
        paymentRepository.save(cancelledPayment);
    }

    @Async
    @Transactional
    public void failPayment(String pid) {
        Payment payment = paymentRepository.findByPid(pid).orElseThrow(
            () -> new CommonException(BError.NOT_EXIST, String.format("Payment %s", pid)));
        Payment failedPayment = payment.failPayment();
        paymentRepository.save(failedPayment);
    }

    private String generatePaymentId() {
        return String.valueOf(UUID.randomUUID());
    }

    private KakaoPayReadyRequest createKakaoPayReadyRequest(User user,
        PaymentRequest paymentRequest, String pid) {
        return KakaoPayReadyRequest.builder()
            .cid("TC0ONETIME")
            .partnerOrderId(paymentRequest.orderId())
            .partnerUserId(user.getUuid())
            .itemName(paymentRequest.itemName())
            .quantity(paymentRequest.quantity())
            .totalAmount(paymentRequest.totalAmount())
            .taxFreeAmount(0)
            .approvalUrl(String.format("http://localhost:8080/api/v1/payment/success/%s", pid))
            .cancelUrl("http://localhost:8080/api/v1/payment/cancel")
            .failUrl("http://localhost:8080/api/v1/payment/fail")
            .build();
    }

    private ResponseEntity<KakaoPayReadyResponse> sendKakaoPayReadyRequest(
        KakaoPayReadyRequest apiRequest) {
        HttpHeaders headers = createHeaders();
        HttpEntity<KakaoPayReadyRequest> request = new HttpEntity<>(apiRequest, headers);

        return restTemplate.exchange(KAKAO_PAY_API_URL, HttpMethod.POST, request,
            KakaoPayReadyResponse.class);
    }

    private String handleKakaoPayReadyResponse(ResponseEntity<KakaoPayReadyResponse> response,
        PaymentRequest paymentRequest, User user, String pid) {
        if (response.getStatusCode() == HttpStatus.OK) {
            KakaoPayReadyResponse responseBody = response.getBody();
            String tid = responseBody.tid();

            Payment payment = Payment.readyPayment(paymentRequest.orderId(), pid, user.getUuid(),
                paymentRequest.totalAmount(), tid);
            paymentRepository.save(payment);

            return responseBody.next_redirect_pc_url();
        } else {
            throw new RuntimeException("Failed to prepare KakaoPay payment");
        }
    }

    private ApproveRequest createApproveRequest(Payment payment, String token) {
        return ApproveRequest.builder()
            .cid(TEST_API_CID)
            .tid(payment.getTid())
            .partnerOrderId(payment.getOrderId())
            .partnerUserId(payment.getUid())
            .pgToken(token)
            .build();
    }

    private ResponseEntity<Map> sendKakaoPayApproveRequest(ApproveRequest approveRequest) {
        HttpHeaders headers = createHeaders();
        HttpEntity<ApproveRequest> request = new HttpEntity<>(approveRequest, headers);
        return restTemplate.exchange(KAKAO_PAY_APPROVE_API_URL, HttpMethod.POST, request,
            Map.class);
    }

    private void handleKakaoPayApproveResponse(Payment payment) {
        Payment approvePayment = payment.approvePayment();
        paymentRepository.save(approvePayment);

    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "SECRET_KEY " + PAY_SECRET_KEY);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}

