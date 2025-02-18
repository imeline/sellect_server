package com.sellect.server.order.repository;

import com.sellect.server.auth.repository.entity.UserEntity;
import com.sellect.server.order.repository.entity.OrderStatus;
import com.sellect.server.order.repository.entity.OrdersEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdersJpaRepository extends JpaRepository<OrdersEntity, Long> {

    List<OrdersEntity> findAllByUserEntityAndStatus(UserEntity user, OrderStatus status);

}
