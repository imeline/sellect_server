package com.sellect.server.order.controller.response;

import com.sellect.server.order.domain.Orders;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailGetResponse(
    String orderNumber,
    BigDecimal discountCost,
    BigDecimal totalPrice,
    List<OrderItemGetResponse> orderItems,
    LocalDateTime updateAt

) {

    public static OrderDetailGetResponse from(Orders order, BigDecimal discountCost,
        List<OrderItemGetResponse> orderItems) {
        return new OrderDetailGetResponse(
            order.getOrderNumber(),
            discountCost,
            order.getTotalPrice(),
            orderItems,
            order.getUpdatedAt()
        );
    }
}
