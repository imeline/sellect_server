package com.sellect.server.order.Infrastructure.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;


@Builder
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public record KakaoPayReadyRequest(
    String cid,
    String partnerOrderId,
    String partnerUserId,
    String itemName,
    Integer quantity,
    Integer totalAmount,
    Integer taxFreeAmount,
    String approvalUrl,
    String cancelUrl,
    String failUrl
) {
    public static KakaoPayReadyRequest of(String partnerOrderId, String partnerUserId,
        String itemName, Integer quantity, Integer totalAmount, String approvalUrl,
        String cancelUrl, String failUrl) {
        return KakaoPayReadyRequest.builder()
            .cid("TC0ONETIME")
            .partnerOrderId(partnerOrderId)
            .partnerUserId(partnerUserId)
            .itemName(itemName)
            .quantity(quantity)
            .totalAmount(totalAmount)
            .taxFreeAmount(0)
            .approvalUrl(approvalUrl)
            .cancelUrl(cancelUrl)
            .failUrl(failUrl)
            .build();
    }
}