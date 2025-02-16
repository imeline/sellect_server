package com.sellect.server.order.repository;

import com.sellect.server.order.domain.OrderItem;
import com.sellect.server.order.repository.entity.OrderItemEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderItemRepositoryImpl implements OrderItemRepository {

    private final OrderItemJpaRepository orderItemJpaRepository;

    @Override
    public List<OrderItem> findAllByOrdersId(Long orderId) {

        List<OrderItemEntity> orderItemEntities = orderItemJpaRepository.findAllByOrdersEntityId(
            orderId);

        return orderItemEntities.stream().map(OrderItemEntity::toModel).toList();
    }
}
