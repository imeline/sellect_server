package com.sellect.server.order.repository;

import com.sellect.server.order.repository.entity.OrderItemEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemJpaRepository extends JpaRepository<OrderItemEntity, Long> {

    List<OrderItemEntity> findAllByOrdersEntityId(Long orderId);
}
