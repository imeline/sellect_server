package com.sellect.server.order.controller.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

public record OrderAddRequest(

    // todo: 총 가격이 String 인 이유 체크
    @Positive(message = "가격은 1원 이상이어야 합니다.")
    @Pattern(regexp = "^[0-9]+$", message = "가격은 정수만 입력 가능합니다.")
    String totalPrice,
    @NotNull(message = "주문 아이템 목록은 필수 값입니다.")
    List<OrderItemAddRequest> orderItems
) {

    public BigDecimal convertPriceAsBigDecimal() {
        return totalPrice != null ? new BigDecimal(totalPrice) : null;
    }
}