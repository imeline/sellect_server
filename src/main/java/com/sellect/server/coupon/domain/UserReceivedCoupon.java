package com.sellect.server.coupon.domain;

import com.sellect.server.auth.domain.User;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class UserReceivedCoupon {
    private final Long id;
    private final User user;
    private final Coupon coupon;
    private final Boolean isUsed;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deleteAt;

    public static UserReceivedCoupon create(User user, Coupon coupon) {
        return UserReceivedCoupon.builder()
            .user(user)
            .coupon(coupon)
            .isUsed(false)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .deleteAt(null)
            .build();
    }

    public UserReceivedCoupon useCoupon() {
            return UserReceivedCoupon.builder()
                .id(this.id)
                .user(this.user)
                .coupon(this.coupon)
                .isUsed(true)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .deleteAt(this.deleteAt)
                .build();
    }
}
