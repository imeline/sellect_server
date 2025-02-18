package com.sellect.server.coupon.repository.entity;

import com.sellect.server.auth.repository.entity.UserEntity;
import com.sellect.server.common.BaseTimeEntity;
import com.sellect.server.coupon.domain.UserReceivedCoupon;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "user_received_coupon")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
public class UserReceivedCouponEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user;
    @ManyToOne(fetch = FetchType.LAZY)
    private CouponEntity coupon;
    private Boolean isUsed;

    public static UserReceivedCouponEntity from(UserReceivedCoupon userReceivedCoupon) {
        return UserReceivedCouponEntity.builder()
            .id(userReceivedCoupon.getId())
            .user(UserEntity.from(userReceivedCoupon.getUser()))
            .coupon(CouponEntity.from(userReceivedCoupon.getCoupon()))
            .isUsed(false)
            .createdAt(userReceivedCoupon.getCreatedAt())
            .updatedAt(userReceivedCoupon.getUpdatedAt())
            .deleteAt(userReceivedCoupon.getDeleteAt())
            .build();

    }
}
