package com.sellect.server.order.repository;

import com.sellect.server.order.domain.OrderItem;
import com.sellect.server.order.repository.entity.OrderItemEntity;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderItemRepositoryImpl implements OrderItemRepository {

    private final OrderItemJpaRepository orderItemJpaRepository;

    @Override
    public List<OrderItem> saveAll(List<OrderItem> orderItems) {
        // OrderItem -> OrderItemEntity로 변환
        List<OrderItemEntity> orderItemEntities = orderItems.stream()
            .map(OrderItemEntity::from)  // OrderItemEntity.from(OrderItem orderItem) 메서드 사용
            .toList();

        List<OrderItemEntity> savedEntities = orderItemJpaRepository.saveAll(orderItemEntities);

        // OrderItemEntity -> OrderItem로 변환
        return savedEntities.stream().map(OrderItemEntity::toModel).toList();
    }

    @Override
    public List<OrderItem> findAllByOrdersId(Long orderId) {

        List<OrderItemEntity> orderItemEntities = orderItemJpaRepository.findAllByOrdersEntityId(
            orderId);

        return orderItemEntities.stream().map(OrderItemEntity::toModel).toList();
    }

    @Override
    public BigDecimal calculateSalesByProductId(Long productId) {
        return orderItemJpaRepository.calculateSalesByProductId(productId);
    }

    @Override
    public Integer countCompleteOrdersByProductId(Long productId) {
        return orderItemJpaRepository.countCompletedOrdersByProductId(productId);
    }

    @Override
    public BigDecimal calculateTotalSalesByProductIds(List<Long> productIds) {
        return orderItemJpaRepository.calculateTotalSalesByProductIds(productIds);
    }
}
