package com.sellect.server.order.application;

import com.sellect.server.auth.domain.User;
import com.sellect.server.cart.domain.CartItem;
import com.sellect.server.cart.repository.CartItemRepository;
import com.sellect.server.common.exception.CommonException;
import com.sellect.server.common.exception.enums.BError;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private final CartItemRepository cartRepository;

    @Transactional
    public Orders registerPendingOrder(User user, OrderAddRequest request) {
        // Orders 저장
        Orders order = Orders.register(user, request.convertPriceAsBigDecimal(),
            OrderStatus.PENDING);
        Orders savedOrder = ordersRepository.save(order);

        // productId 중복 확인을 위해 set
        Set<Long> productIds = new HashSet<>();

        // OrderAddRequest -> OrderItemAddRequest -> OrderItem 으로 변환
        List<OrderItem> orderItems = request.orderItems().stream()
            .map(orderItemAddRequest -> {
                if (!productIds.add(orderItemAddRequest.productId())) {
                    throw new CommonException(BError.EXIST, "productId");
                }

                Product product = productRepository.findById(orderItemAddRequest.productId())
                    .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "product"));

                return OrderItem.register(
                    savedOrder,
                    product,
                    orderItemAddRequest.convertPriceAsBigDecimal(),
                    orderItemAddRequest.quantity()
                );
            })
            .toList();
        // OrderItem 저장
        orderItemRepository.saveAll(orderItems);

        return savedOrder;
    }

    @Transactional
    public void LockProductItems(Long orderId) {
        List<OrderItem> orderItems = getOrderItemsByOrderId(orderId);

        orderItems.forEach(orderItem -> {
            // 재고 확인
            if (orderItem.getProduct().getStock() < orderItem.getQuantity()) {
                throw new CommonException(BError.NOT_VALID, "재고 부족");
            }
            // DB 락
            productRepository.findByIdWithLock(orderItem.getProduct().getId())
                .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "상품이 존재하지 않습니다."));
        });
    }

    @Transactional
    public void completeOrder(User user, Long orderId) {
        List<OrderItem> orderItems = getOrderItemsByOrderId(orderId);

        orderItems.forEach(orderItem -> {
            // 재고 차감
            productRepository.save(orderItem.getProduct().updateStock(orderItem.getQuantity()));
        });

        // 주문 확정
        Orders order = getOrderById(orderId);
        order = ordersRepository.save(order.updateStatus(OrderStatus.COMPLETED));

        // 쿠폰 삭제, 장바구니 비우기
        clearCartAndDeleteCouponAsync(user, order);
    }

    // 비동기 처리
    @Async
    public void clearCartAndDeleteCouponAsync(User user, Orders order) {
        clearCartAndDeleteCoupons(user, order);
    }

    @Transactional
    public void clearCartAndDeleteCoupons(User user, Orders order) {
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new CommonException(BError.NOT_VALID, "주문이 완료되지 않았습니다.");
        }
        // 장바구니 비우기
        List<CartItem> cartItems = cartRepository.findAllByUserId(user.getId());
        cartItems.forEach(CartItem::remove);
        cartRepository.saveAll(cartItems);

        // 쿠폰 삭제
        //couponRepository.deleteByUser(order.getUserReceivedCoupon.getId());
    }


    public List<OrderGetResponse> getOrdersByUser(User user) {
        // 완료된 주문만 조회
        List<Orders> orderList = ordersRepository.findAllByUserEntityAndStatus(user,
            OrderStatus.COMPLETED);
        // 주문 목록이 없을 경우
        if (orderList.isEmpty()) {
            throw new CommonException(BError.NOT_EXIST, "주문 목록");
        }
        return orderList.stream()
            .map(order -> OrderGetResponse.from(order, getOrderItemsByOrderId(order.getId())))
            .toList();
    }

    public OrderDetailGetResponse getOrderDetail(Long orderId) {

        Orders order = getOrderById(orderId);
        // PENDING 상태 주문은 에러 처리
        if (order.getStatus() == OrderStatus.PENDING) {
            throw new CommonException(BError.NOT_VALID, "PENDING 상태의 주문은 조회할 수 없습니다.");
        }

        List<OrderItem> orderItems = getOrderItemsByOrderId(orderId);

        return OrderDetailGetResponse.from(order, orderItems);
    }

    @Transactional(readOnly = true)
    protected List<OrderItem> getOrderItemsByOrderId(Long orderId) {
        List<OrderItem> orderItems = orderItemRepository.findAllByOrdersId(orderId);
        // 주문 아이템이 없을 경우
        if (orderItems.isEmpty()) {
            throw new CommonException(BError.NOT_EXIST, "주문 아이템");
        }
        return orderItems;
    }

    @Transactional(readOnly = true)
    public Orders getOrderById(Long orderId) {
        return ordersRepository.findById(orderId)
            .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "주문"));
    }
}
