package com.sellect.server.order.repository.entity;

import com.sellect.server.auth.repository.entity.UserEntity;
import com.sellect.server.common.BaseTimeEntity;
import com.sellect.server.order.domain.Orders;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrdersEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_received_coupon_id", nullable = true)
//    private UserReceivedCouponEntity userReceivedCouponEntity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(nullable = false, length = 50)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    public Orders toModel() {
        return Orders.builder()
            .id(id)
            .user(userEntity.toModel())
//            .userReceivedCoupon(userReceivedCouponEntity.toModel())
            .totalPrice(totalPrice)
            .orderNumber(orderNumber)
            .status(status)
            .createdAt(getCreatedAt())
            .updatedAt(getUpdatedAt())
            .build();
    }
}
