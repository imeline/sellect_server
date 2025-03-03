package com.sellect.server.order.repository;

import com.sellect.server.order.domain.OrderItem;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OrderItemRepository {

    List<OrderItem> saveAll(List<OrderItem> orderItems);

    List<OrderItem> findAllByOrdersId(Long orderId);

    Optional<BigDecimal> calculateSalesByProductId(Long productId);

    Optional<Integer> countCompleteOrdersByProductId(Long productId);

    Optional<BigDecimal> calculateTotalSalesByProductIds(List<Long> productIds);
}
