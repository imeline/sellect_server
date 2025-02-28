package com.sellect.server.order.Infrastructure.port;

import com.sellect.server.order.Infrastructure.request.KakaoPayReadyRequest;
import com.sellect.server.order.Infrastructure.response.KakaoPayReadyResponse;
import com.sellect.server.payment.controller.request.ApproveRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class KakaoPayClient {

    @Value("${kakao.pay.secret-key}")
    private String PAY_SECRET_KEY;
    private static final String KAKAO_PAY_API_URL = "https://open-api.kakaopay.com/online/v1/payment/ready";
    private static final String KAKAO_PAY_APPROVE_API_URL = "https://open-api.kakaopay.com/online/v1/payment/approve";

    private final RestTemplate restTemplate;

    // ready
    public KakaoPayReadyResponse readyPayment(KakaoPayReadyRequest request) {
        HttpHeaders headers = createHeaders();
        HttpEntity<KakaoPayReadyRequest> readyRequest = new HttpEntity<>(request, headers);
        ResponseEntity<KakaoPayReadyResponse> response = restTemplate.exchange(KAKAO_PAY_API_URL,
            HttpMethod.POST, readyRequest,
            KakaoPayReadyResponse.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("카카오페이 결제 준비 실패");
        }

        return response.getBody();
    }

    // approve
    public Map paymentApprove(ApproveRequest approveRequest) {
        HttpHeaders headers = createHeaders();
        HttpEntity<ApproveRequest> request = new HttpEntity<>(approveRequest, headers);
        ResponseEntity<Map> response = restTemplate.exchange(KAKAO_PAY_APPROVE_API_URL,
            HttpMethod.POST, request, Map.class);

        return response.getBody();
    }



    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "SECRET_KEY " + PAY_SECRET_KEY);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
