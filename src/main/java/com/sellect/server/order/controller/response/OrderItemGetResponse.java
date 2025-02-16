package com.sellect.server.order.controller.response;

import com.sellect.server.order.domain.OrderItem;
import java.math.BigDecimal;
import java.util.List;

public record OrderItemGetResponse(
    Long productId,
    String productName,
    BigDecimal productPrice,
    int quantity

) {

    public static OrderItemGetResponse from(OrderItem orderItem) {
        return new OrderItemGetResponse(
            orderItem.getProduct().getId(),
            orderItem.getProduct().getName(),
            orderItem.getProduct().getPrice(),
            orderItem.getQuantity()
        );
    }

    public static List<OrderItemGetResponse> fromList(List<OrderItem> orderItems) {
        return orderItems.stream()
            .map(OrderItemGetResponse::from)
            .toList();
    }
}
