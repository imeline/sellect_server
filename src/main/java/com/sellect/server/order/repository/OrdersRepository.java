package com.sellect.server.order.repository;

import com.sellect.server.auth.domain.User;
import com.sellect.server.order.domain.Orders;
import com.sellect.server.order.repository.entity.OrderStatus;
import java.util.List;
import java.util.Optional;

public interface OrdersRepository {

    Orders save(Orders orders);

    Optional<Orders> findById(Long id);

    List<Orders> findCompletedOrdersByUser(User user, OrderStatus status);
}
