package com.sellect.server.order.repository;

import com.sellect.server.auth.domain.User;
import com.sellect.server.auth.repository.entity.UserEntity;
import com.sellect.server.order.domain.Orders;
import com.sellect.server.order.repository.entity.OrdersEntity;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrdersRepositoryImpl implements OrdersRepository {

    private final OrdersJpaRepository ordersJpaRepository;

    @Override
    public Orders save(Orders orders) {
        return ordersJpaRepository.save(OrdersEntity.from(orders)).toModel();
    }

    @Override
    public Optional<Orders> findById(Long id) {
        return ordersJpaRepository.findById(id).map(OrdersEntity::toModel);
    }

    @Override
    public List<Orders> findAllByUser(User user) {
        List<OrdersEntity> ordersEntities = ordersJpaRepository.findAllByUserEntity(
            UserEntity.from(user));

        return ordersEntities.stream().map(OrdersEntity::toModel).toList();
    }
}
