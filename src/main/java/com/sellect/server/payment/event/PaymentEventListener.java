package com.sellect.server.payment.event;

import com.sellect.server.auth.domain.User;
import com.sellect.server.order.Infrastructure.port.KakaoPayClient;
import com.sellect.server.order.Infrastructure.request.KakaoPayReadyRequest;
import com.sellect.server.order.Infrastructure.response.KakaoPayApproveResponse;
import com.sellect.server.order.Infrastructure.response.KakaoPayReadyResponse;
import com.sellect.server.order.domain.Orders;
import com.sellect.server.payment.controller.request.ApproveRequest;
import com.sellect.server.payment.domain.Payment;
import com.sellect.server.payment.repository.PaymentRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentEventListener {
    private final KakaoPayClient kakaoPayClient;
    private final PaymentRepository paymentRepository;

    @Async("paymentTaskExecutor")
    @EventListener
    public void kakaoPayReadyEvent(KakaoPayReadyEvent event){
        try {
            String pid = String.valueOf(UUID.randomUUID());
            Integer quantity = 0;
            KakaoPayReadyRequest request = kakaoPayClient.createKakaoPayReadyRequest(
                String.valueOf(event.getOrderId()),
                event.getUser().getUuid(),
                "test",
                quantity,
                event.getOrder().getTotalPrice().intValue(),
                pid
            );

            KakaoPayReadyResponse response = kakaoPayClient.readyPayment(request);

            Payment payment = Payment.ready(
                String.valueOf(event.getOrderId()),
                pid,
                event.getUser().getUuid(),
                event.getOrder().getTotalPrice().intValue(),
                response.tid()
            );
            paymentRepository.save(payment);

            event.getFuture().complete(response.next_redirect_pc_url());
        } catch (Exception e) {
            event.getFuture().completeExceptionally(e);
        }
    }


    @Async("paymentTaskExecutor")
    @EventListener
    public void kakaoPayApproveEvent(KakaoPayApproveEvent event){

        Payment approvePayment = event.getPayment().approvePayment();
        paymentRepository.save(approvePayment);

        // 카카오 한테 요청 보내기
        ApproveRequest approveRequest = ApproveRequest.builder()
            .cid("TC0ONETIME")
            .tid(approvePayment.getTid())
            .partnerOrderId(approvePayment.getOrderId())
            .partnerUserId(approvePayment.getUid())
            .pgToken(event.getToken())
            .build();

        KakaoPayApproveResponse kakaoPayApproveResponse = kakaoPayClient.paymentApprove(
            approveRequest);
    }
}
