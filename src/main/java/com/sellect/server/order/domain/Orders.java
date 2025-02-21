package com.sellect.server.order.domain;

import com.sellect.server.auth.domain.User;
import com.sellect.server.coupon.domain.UserReceivedCoupon;
import com.sellect.server.order.repository.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Orders {

    private final Long id;

    private final User user;

    private final UserReceivedCoupon userReceivedCoupon;

    private final BigDecimal totalPrice;

    private final String orderNumber;

    private final OrderStatus status;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    private final LocalDateTime deletedAt;

    public static Orders register(User user, BigDecimal totalPrice,
        OrderStatus status) {
        return Orders.builder()
            .user(user)
            .totalPrice(totalPrice)
            // 주문 번호 UUID 생성 (하이픈 제거, 대문자 + 숫자 조합)
            .orderNumber(UUID.randomUUID().toString().replace("-", "").toUpperCase())
            .status(status)
            .build();
    }

    public Orders updateStatus(OrderStatus status) {
        return Orders.builder()
            .id(this.id)
            .user(this.user)
            .userReceivedCoupon(this.userReceivedCoupon)
            .totalPrice(this.totalPrice)
            .orderNumber(this.orderNumber)
            .status(status)
            .createdAt(this.createdAt)
            .updatedAt(LocalDateTime.now())
            .deletedAt(this.deletedAt)
            .build();
    }

    public Orders updateCoupon(UserReceivedCoupon coupon) {
        return Orders.builder()
            .id(this.id)
            .user(this.user)
            .userReceivedCoupon(coupon)
            // 할인 가격 적용
            .totalPrice(
                this.totalPrice.subtract(BigDecimal.valueOf(coupon.getCoupon().getDiscountCost())))
            .orderNumber(this.orderNumber)
            .status(this.status)
            .createdAt(this.createdAt)
            .updatedAt(LocalDateTime.now())
            .deletedAt(this.deletedAt)
            .build();
    }
}
