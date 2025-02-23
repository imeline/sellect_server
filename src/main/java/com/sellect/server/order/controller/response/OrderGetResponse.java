package com.sellect.server.order.controller.response;

import com.sellect.server.order.domain.Orders;
import java.time.LocalDateTime;
import java.util.List;

public record OrderGetResponse(
    Long orderId,
    List<OrderItemGetResponse> orderItems,
    LocalDateTime updateAt

) {

    public static OrderGetResponse from(Orders order, List<OrderItemGetResponse> orderItems) {
        return new OrderGetResponse(
            order.getId(),
            orderItems,
            order.getUpdatedAt()
        );
    }
}
