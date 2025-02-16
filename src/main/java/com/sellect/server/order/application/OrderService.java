package com.sellect.server.order.application;

import com.sellect.server.auth.domain.User;
import com.sellect.server.order.controller.request.OrderAddRequest;
import com.sellect.server.order.controller.response.OrderDetailGetResponse;
import com.sellect.server.order.controller.response.OrderGetResponse;
import com.sellect.server.order.domain.OrderItem;
import com.sellect.server.order.domain.Orders;
import com.sellect.server.order.repository.OrderItemRepository;
import com.sellect.server.order.repository.OrdersRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderService {

    private final OrdersRepository ordersRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional
    public long addOrderPending(User user, OrderAddRequest request) {

    }

    public List<OrderGetResponse> getOrdersByUser(User user) {
        List<Orders> orderList = ordersRepository.findAllByUser(user);
        // 주문 목록이 없을 경우
        if (orderList.isEmpty()) {
            throw new IllegalArgumentException("No orders found for the user.");
        }

        return orderList.stream()
            .map(order -> {
                List<OrderItem> orderItems = orderItemRepository.findAllByOrdersId(order.getId());
                // 주문 아이템이 없을 경우
                if (orderItems.isEmpty()) {
                    throw new IllegalArgumentException(
                        "No items found for order with ID: " + order.getId());
                }
                return OrderGetResponse.from(order, orderItems);
            })
            .toList();
    }

    public OrderDetailGetResponse getOrderDetail(User user, Long orderId) {

        Orders order = ordersRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // 주문 user와 로그인 user가 같은지 확인
        if (!order.getUser().equals(user)) { // user_id 가 같으면 true
            throw new IllegalArgumentException("User does not have permission to view this order.");
        }

        List<OrderItem> orderItems = orderItemRepository.findAllByOrdersId(order.getId());
        // 주문 아이템이 없을 경우
        if (orderItems.isEmpty()) {
            throw new IllegalArgumentException(
                "No items found for order with ID: " + order.getId());
        }

        // 할인 비용 (userReceivedCoupon에서 discountCost를 가져옴)
//        BigDecimal discountCost = (order.getUserReceivedCoupon() != null)
//            ? order.getUserReceivedCoupon().getDiscountCost()
//            : BigDecimal.ZERO;

        return OrderDetailGetResponse.from(order, discountCost, orderItems);
    }

}
