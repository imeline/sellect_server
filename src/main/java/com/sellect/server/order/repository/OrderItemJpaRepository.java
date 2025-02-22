package com.sellect.server.order.repository;

import com.sellect.server.order.repository.entity.OrderItemEntity;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemJpaRepository extends JpaRepository<OrderItemEntity, Long> {

    List<OrderItemEntity> findAllByOrdersEntityId(Long orderId);

    @Query("SELECT SUM(o.totalPrice) "
        + "FROM OrderItemEntity oi "
        + "JOIN oi.ordersEntity o "
        + "WHERE oi.productEntity.id = :productId "
        + "AND o.status = 'COMPLETED'")
    BigDecimal calculateTotalSalesByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(DISTINCT o.id) "
        + "FROM OrderItemEntity oi "
        + "JOIN oi.ordersEntity o "
        + "WHERE oi.productEntity.id = :productId "
        + "AND o.status = 'COMPLETED'")
    Integer countCompletedOrdersByProductId(@Param("productId") Long productId);
}
