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
    private Long id;
    private User user;
    private Coupon coupon;
    private Boolean isUsed;
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
}
