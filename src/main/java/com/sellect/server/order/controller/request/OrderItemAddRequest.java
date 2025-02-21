package com.sellect.server.order.controller.request;

import com.sellect.server.order.controller.response.OrderItemGetResponse;
import com.sellect.server.order.domain.OrderItem;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record OrderItemAddRequest(
    Long productId,
    @Positive(message = "가격은 1원 이상이어야 합니다.")
//    @Pattern(regexp = "^[0-9]+$", message = "가격은 정수만 입력 가능합니다.")
    String price,
    @Positive(message = "수량은 1개 이상이어야 합니다.")
    int quantity
) {

    public BigDecimal convertPriceAsBigDecimal() {
        return price != null ? new BigDecimal(price) : null;
    }

    public static OrderItemGetResponse from(OrderItem orderItem) {
        return new OrderItemGetResponse(
            orderItem.getProduct().getId(),
            orderItem.getProduct().getName(),
            orderItem.getProduct().getPrice(),
            orderItem.getQuantity()
        );
    }
}
