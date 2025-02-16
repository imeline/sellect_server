package com.sellect.server.order.domain;

import com.sellect.server.auth.domain.User;
import com.sellect.server.order.repository.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Orders {

    private final long id;

    private final User user;

//    private final UserReceivedCoupon userReceivedCoupon;

    private final BigDecimal totalPrice;

    private final String orderNumber;

    private final OrderStatus status;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    private final LocalDateTime deletedAt;
}
