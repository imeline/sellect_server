package com.sellect.server.order.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.sellect.server.auth.domain.User;
import com.sellect.server.coupon.domain.Coupon;
import com.sellect.server.coupon.domain.UserReceivedCoupon;
import com.sellect.server.order.repository.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OrdersTest {

    private User user;
    private UserReceivedCoupon userReceivedCoupon;
    private BigDecimal totalPrice;
    private String orderNumber;
    private LocalDateTime time;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .id(1L)
            .build();

        Coupon coupon = Coupon.builder()
            .id(1L)
            .discountCost(1000)
            .build();

        userReceivedCoupon = UserReceivedCoupon.builder()
            .id(1L)
            .user(user)
            .coupon(coupon)
            .build();

        totalPrice = new BigDecimal("100000");
        time = LocalDateTime.parse("2024-08-01T00:00:00");
        orderNumber = "test-order-number";
    }

    @Test
    void testRegister() {
        // when
        Orders order = Orders.register(user, totalPrice, OrderStatus.PENDING);

        // then
        assertNotNull(order);
        assertEquals(user, order.getUser());
        assertEquals(totalPrice, order.getTotalPrice());
        assertNotNull(order.getOrderNumber());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertNull(order.getDeletedAt());
    }

    @Test
    void testUpdateStatus() {
        // given
        Orders order = Orders.builder()
            .id(1L)
            .user(user)
            .userReceivedCoupon(userReceivedCoupon)
            .totalPrice(totalPrice)
            .orderNumber(orderNumber)
            .status(OrderStatus.PENDING)
            .createdAt(time)
            .updatedAt(time)
            .build();

        // when
        OrderStatus newStatus = OrderStatus.COMPLETED;
        Orders updatedOrder = order.updateStatus(newStatus);

        // then
        assertNotNull(updatedOrder);
        assertEquals(order.getId(), updatedOrder.getId());
        assertEquals(order.getUser(), updatedOrder.getUser());
        assertEquals(order.getUserReceivedCoupon(), updatedOrder.getUserReceivedCoupon());
        assertEquals(order.getTotalPrice(), updatedOrder.getTotalPrice());
        assertEquals(order.getOrderNumber(), updatedOrder.getOrderNumber());
        assertEquals(newStatus, updatedOrder.getStatus()); // 상태가 변경되었는지 확인
        assertEquals(order.getCreatedAt(), updatedOrder.getCreatedAt());
        assertNotEquals(order.getUpdatedAt(), updatedOrder.getUpdatedAt()); // 업데이트 시간이 변경되었는지 확인
        assertEquals(order.getDeletedAt(), updatedOrder.getDeletedAt());
    }

    @Test
    void testUpdateCoupon() {
        // given
        Orders order = Orders.builder()
            .id(1L)
            .user(user)
            .userReceivedCoupon(null)
            .totalPrice(totalPrice)
            .orderNumber(orderNumber)
            .status(OrderStatus.PENDING)
            .createdAt(time)
            .updatedAt(time)
            .build();

        // when
        Orders updatedOrder = order.updateCoupon(userReceivedCoupon);
        int discountCost = userReceivedCoupon.getCoupon().getDiscountCost();

        // then
        assertNotNull(updatedOrder); // 업데이트된 주문 객체가 생성되었는지 확인
        assertEquals(order.getId(), updatedOrder.getId());
        assertEquals(order.getUser(), updatedOrder.getUser());
        assertEquals(userReceivedCoupon, updatedOrder.getUserReceivedCoupon()); // 쿠폰 정보가 설정되었는지 확인
        BigDecimal minusCouponPrice = order.getTotalPrice()
            .subtract(BigDecimal.valueOf(discountCost));
        assertEquals(minusCouponPrice, updatedOrder.getTotalPrice());
        assertEquals(order.getOrderNumber(), updatedOrder.getOrderNumber());
        assertEquals(order.getStatus(), updatedOrder.getStatus());
        assertEquals(order.getCreatedAt(), updatedOrder.getCreatedAt());
        assertNotEquals(order.getUpdatedAt(), updatedOrder.getUpdatedAt()); // 생성 시간이 변경되었는지 확인
        assertEquals(order.getDeletedAt(), updatedOrder.getDeletedAt());
    }
}