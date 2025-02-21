package com.sellect.server.order.controller.response;

import com.sellect.server.order.domain.OrderItem;
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
        List<OrderItem> orderItems) {

        List<OrderItemGetResponse> orderItemResponses = OrderItemGetResponse.fromList(orderItems);
        return new OrderDetailGetResponse(
            order.getOrderNumber(),
            discountCost,
            order.getTotalPrice(),
            orderItemResponses,
            order.getUpdatedAt()
        );
    }
}
