package com.sellect.server.order.repository;

import com.sellect.server.order.domain.OrderItem;
import java.math.BigDecimal;
import java.util.List;

public interface OrderItemRepository {

    List<OrderItem> saveAll(List<OrderItem> orderItems);

    List<OrderItem> findAllByOrdersId(Long orderId);

    BigDecimal calculateTotalSalesByProductId(Long orderItemId);

    Integer countCompleteOrdersByProductId(Long productId);
}
