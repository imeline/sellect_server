package com.sellect.server.order.controller.response;

import com.sellect.server.order.domain.OrderItem;
import com.sellect.server.product.domain.Product;
import java.math.BigDecimal;

public record OrderItemPendingReadResponse(
    Long productId,
    String brandName,
    String productName,
    BigDecimal productPrice,
    int quantity,
    String imageUrl

) {

    public static OrderItemPendingReadResponse from(OrderItem orderItem, Product product, String imageUrl) {
        return new OrderItemPendingReadResponse(
            orderItem.getProduct().getId(),
            product.getBrand().getName(), // todo: N+1 발생 원인 부분 (임시로 일단 구현에 집중)
            orderItem.getProduct().getName(),
            orderItem.getProduct().getPrice(),
            orderItem.getQuantity(),
            imageUrl
        );
    }
}
