package com.sellect.server.order.repository;

import com.sellect.server.order.domain.OrderItem;
import java.util.List;

public interface OrderItemRepository {

    List<OrderItem> findAllByOrdersId(Long orderId);
}
