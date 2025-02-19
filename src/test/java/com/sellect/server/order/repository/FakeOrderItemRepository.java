package com.sellect.server.order.repository;

import com.sellect.server.order.domain.OrderItem;
import java.util.ArrayList;
import java.util.List;

public class FakeOrderItemRepository implements OrderItemRepository {

    private final List<OrderItem> data = new ArrayList<>();
    private long idSequence = 1L;

    @Override
    public List<OrderItem> saveAll(List<OrderItem> orderItems) {
        List<OrderItem> mutableOrderItems = new ArrayList<>();
        for (OrderItem item : orderItems) {
            if (item.getId() == null) {
                mutableOrderItems.add(OrderItem.builder()
                    .id(idSequence++)
                    .orders(item.getOrders())
                    .product(item.getProduct())
                    .price(item.getPrice())
                    .quantity(item.getQuantity())
                    .build());
            } else {
                mutableOrderItems.add(item);
            }
        }
        data.addAll(mutableOrderItems);
        return new ArrayList<>(mutableOrderItems);
    }

    @Override
    public List<OrderItem> findAllByOrdersId(Long orderId) {
        return data.stream()
            .filter(item -> item.getOrders().getId().equals(orderId))
            .toList();
    }

    public void clear() {
        data.clear();
    }
}
