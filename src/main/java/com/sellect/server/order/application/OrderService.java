package com.sellect.server.order.application;

import com.sellect.server.auth.domain.User;
import com.sellect.server.order.controller.request.OrderAddRequest;
import com.sellect.server.order.controller.response.OrderDetailGetResponse;
import com.sellect.server.order.controller.response.OrderGetResponse;
import com.sellect.server.order.domain.OrderItem;
import com.sellect.server.order.domain.Orders;
import com.sellect.server.order.repository.OrderItemRepository;
import com.sellect.server.order.repository.OrdersRepository;
import com.sellect.server.order.repository.entity.OrderStatus;
import com.sellect.server.product.domain.Product;
import com.sellect.server.product.repository.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderService {

    private final OrdersRepository ordersRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    // private final CartRepository cartRepository;

    @Transactional
    public Long registerPendingOrder(User user, OrderAddRequest request) {
        Orders order = Orders.register(user, request.convertPriceAsBigDecimal(),
            OrderStatus.PENDING);
        Orders savedOrder = ordersRepository.save(order);

        // OrderAddRequest -> OrderItemAddRequest -> OrderItem 으로 변환
        List<OrderItem> orderItems = request.orderItems().stream()
            .map(orderItemAddRequest -> {

                Product product = productRepository.findById(orderItemAddRequest.productId())
                    .orElseThrow(() -> new RuntimeException("상품이 존제하지 않습니다."));

                return OrderItem.register(
                    savedOrder,
                    product,
                    orderItemAddRequest.convertPriceAsBigDecimal(),
                    orderItemAddRequest.quantity()
                );
            })
            .toList();

        orderItemRepository.saveAll(orderItems);

        return savedOrder.getId();
    }

    @Transactional
    public void completeOrder(User user, Long orderId) {

        List<OrderItem> orderItems = getOrderItemsByOrderId(orderId);

        orderItems.forEach(orderItem -> {
            // DB 락
            Product product = productRepository.findByIdWithLock(orderItem.getProduct().getId())
                .orElseThrow(() -> new RuntimeException("상품이 존재하지 않습니다."));
            // 재고 확인 및 예약(차감)
            productRepository.save(product.updateStock(orderItem.getQuantity()));
        });

        // 결제
//        if (!paymentService.processPayment(order.getTotalPrice())) {
//            throw new IllegalStateException("Payment failed");
//        }

        // 주문 확정
        Orders order = getOrderById(orderId);
        ordersRepository.save(order.updateStatus(OrderStatus.COMPLETED));

        // 쿠폰 삭제, 장바구니 비우기
        clearCartAndDeleteCouponAsync(user, order);
    }

    // 비동기 처리
    @Async
    public void clearCartAndDeleteCouponAsync(User user, Orders order) {
        clearCartAndDeleteCoupons(user, order);
    }

    // completeOrder 과 트랜잭션 분리 - DB 락 시간을 줄이기 위함
    @Transactional
    public void clearCartAndDeleteCoupons(User user, Orders order) {
        // 장바구니 비우기
        //cartRepository.clearCart(user.getId());

        // 쿠폰 삭제
        //couponRepository.deleteByUser(order.getUserReceivedCoupon.getId());
    }


    public List<OrderGetResponse> getOrdersByUser(User user) {
        List<Orders> orderList = ordersRepository.findAllByUser(user);
        // 주문 목록이 없을 경우
        if (orderList.isEmpty()) {
            throw new IllegalArgumentException("해당 회원의 주문 목록이 없습니다.");
        }

        return orderList.stream()
            .map(order -> OrderGetResponse.from(order, getOrderItemsByOrderId(order.getId())))
            .toList();
    }

    public OrderDetailGetResponse getOrderDetail(Long orderId) {

        Orders order = getOrderById(orderId);
        List<OrderItem> orderItems = getOrderItemsByOrderId(orderId);

        // 할인 비용 (userReceivedCoupon에서 discountCost를 가져옴)
//        BigDecimal discountCost = (order.getUserReceivedCoupon() != null)
//            ? order.getUserReceivedCoupon().getDiscountCost()
//            : BigDecimal.ZERO;

        return OrderDetailGetResponse.from(order/*, discountCost*/, orderItems);
    }

    @Transactional(readOnly = true)
    protected List<OrderItem> getOrderItemsByOrderId(Long orderId) {
        List<OrderItem> orderItems = orderItemRepository.findAllByOrdersId(orderId);
        // 주문 아이템이 없을 경우
        if (orderItems.isEmpty()) {
            throw new IllegalArgumentException("해당 주문 id의 주문 아이템이 없습니다.");
        }
        return orderItems;
    }

    @Transactional(readOnly = true)
    public Orders getOrderById(Long orderId) {
        return ordersRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("해당 주문 id의 주문이 없습니다."));
    }

}
