package com.sellect.server.order.application;

import com.sellect.server.auth.domain.User;
import com.sellect.server.auth.repository.user.UserRepository;
import com.sellect.server.cart.domain.CartItem;
import com.sellect.server.cart.repository.CartItemRepository;
import com.sellect.server.common.exception.CommonException;
import com.sellect.server.common.exception.enums.BError;
import com.sellect.server.coupon.domain.UserReceivedCoupon;
import com.sellect.server.coupon.repository.UserReceivedCouponRepository;
import com.sellect.server.order.domain.OrderItem;
import com.sellect.server.order.domain.Orders;
import com.sellect.server.order.repository.OrderItemRepository;
import com.sellect.server.order.repository.OrdersRepository;
import com.sellect.server.order.repository.entity.OrderStatus;
import com.sellect.server.payment.application.PaymentServiceV0;
import com.sellect.server.payment.domain.Payment;
import com.sellect.server.product.domain.Inventory;
import com.sellect.server.product.domain.Product;
import com.sellect.server.product.repository.InventoryRepository;
import com.sellect.server.product.repository.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceV0 {

    private final InventoryRepository inventoryRepository;
    private final CartItemRepository cartRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrdersRepository ordersRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PaymentServiceV0 paymentService;
    private final UserReceivedCouponRepository userReceivedCouponRepository;


    @Transactional
    public String payOrder(User user, Long orderId, Long userReceivedCouponId) {
        Orders order = getOrderById(orderId);
        order.validateOwner(user);

        // 쿠폰 적용
        if (userReceivedCouponId != null) {
            UserReceivedCoupon coupon = userReceivedCouponRepository.findById(userReceivedCouponId)
                .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "쿠폰"));
            order = ordersRepository.save(order.applyCoupon(coupon));
        }

        // 결제 요청
        return paymentService.getKakaoPayReadyResponse(user, orderId, order);
    }


    @Transactional
    public void approvePayment(String pid, String token) {
        Payment payment = paymentService.findReadyPaymentByPid(pid);
        try {
            // order
            // start
            Long orderId = Long.valueOf(payment.getOrderId());
            User user = userRepository.findByUuid(payment.getUid())
                .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "user"));

            Orders order = getOrderById(orderId);
            // 이미 완료된 주문인지 확인
            if (order.getStatus() == OrderStatus.COMPLETED) {
                throw new CommonException(BError.NOT_VALID, "이미 완료(확정)된 주문입니다.");
            }
            List<OrderItem> orderItems = getOrderItemsByOrderId(orderId);


            List<Inventory> deductedInventories = orderItems.stream()
                .map(orderItem -> {
                    Product product = orderItem.getProduct();
                    // DB 락
                    Inventory inventory = inventoryRepository.findWithLockByProductId(product.getId())
                        .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "inventory"));
                    // 재고 확인 및 차감
                    return orderItem.deductStock(inventory);
                })
                .toList();
            deductedInventories.forEach(inventoryRepository::save);
            Orders savedOrder = ordersRepository.save(order.changeStatus(OrderStatus.COMPLETED));

            // 쿠폰 사용 처리, 장바구니 비우기
            clearCartAndDeleteCouponAsync(user, savedOrder);

            paymentService.paymentApprove(pid, token, payment);
        } catch (Exception e) {
            log.error("Failed to approve payment for pid: {}", pid, e);
        }
    }

    private List<OrderItem> getOrderItemsByOrderId(Long orderId) {
        List<OrderItem> orderItems = orderItemRepository.findAllByOrdersId(orderId);
        if (orderItems.isEmpty()) {
            throw new CommonException(BError.NOT_EXIST, "주문 아이템");
        }
        return orderItems;
    }

    @Async
    public void clearCartAndDeleteCouponAsync(User user, Orders order) {
        clearCartAndDeleteCoupon(user, order);
    }

    @Transactional
    public void clearCartAndDeleteCoupon(User user, Orders order) {
        order.validateCompleted();

        List<CartItem> cartItems = cartRepository.findAllByUserId(user.getId());
        List<CartItem> removedCartItems = cartItems.stream()
            .map(CartItem::remove)
            .toList();
        cartRepository.saveAll(removedCartItems);

        if (order.getUserReceivedCoupon() != null) {
            userReceivedCouponRepository.save(order.getUserReceivedCoupon().useCoupon());
        }
    }

    private Orders getOrderById(Long orderId) {
        return ordersRepository.findById(orderId)
            .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "주문"));
    }

}
