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

    private Long id;
    private User seller;
    private Integer discountCost;
    private Integer quantity;
    private LocalDate expirationDate;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private LocalDateTime deleteAt;

    public void decreaseQuantity() {
        this.quantity--;
    }

}
