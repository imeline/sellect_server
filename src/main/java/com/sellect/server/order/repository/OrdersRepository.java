package com.sellect.server.order.repository;

import com.sellect.server.auth.domain.User;
import com.sellect.server.order.domain.Orders;
import java.util.List;
import java.util.Optional;

public interface OrdersRepository {

    Optional<Orders> findById(Long id);

    List<Orders> findAllByUser(User user);
}
