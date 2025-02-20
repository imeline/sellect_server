package com.sellect.server.order.controller.response;

import com.sellect.server.order.domain.OrderItem;
import com.sellect.server.order.domain.Orders;
import java.time.LocalDateTime;
import java.util.List;

public record OrderGetResponse(
    Long orderId,
    List<OrderItemGetResponse> orderItems,
    LocalDateTime createdAt

) {

    public static OrderGetResponse from(Orders order, List<OrderItem> orderItems) {
        List<OrderItemGetResponse> orderItemResponses = OrderItemGetResponse.fromList(
            orderItems);
        return new OrderGetResponse(
            order.getId(),
            orderItemResponses,
            order.getCreatedAt()
        );
    }
}
