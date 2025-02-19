package com.sellect.server.coupon.domain;

import com.sellect.server.auth.domain.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {

    private final Long id;
    private final User seller;
    private final Integer discountCost;
    private final Integer quantity;
    private final LocalDate expirationDate;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deleteAt;

    public Coupon decreaseQuantity() {
        return Coupon.builder()
            .id(this.id)
            .seller(this.seller)
            .discountCost(this.discountCost)
            .quantity(this.quantity - 1)
            .expirationDate(this.expirationDate)
            .createdAt(this.createdAt)
            .updatedAt(LocalDateTime.now())
            .deleteAt(this.deleteAt)
            .build();
    }

}
