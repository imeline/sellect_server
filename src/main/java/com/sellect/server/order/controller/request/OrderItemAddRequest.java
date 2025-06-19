package com.sellect.server.order.controller.request;

import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record OrderItemAddRequest(
    Long productId,
    @Positive(message = "가격은 1원 이상이어야 합니다.")
    String price,
    @Positive(message = "수량은 1개 이상이어야 합니다.")
    int quantity
) {

    public BigDecimal convertPriceAsBigDecimal() {
        return price != null ? new BigDecimal(price) : null;
    }
}
