package com.sellect.server.coupon.controller.response;


import java.time.LocalDate;

public record CouponPossibleOrderResponse(
    Long userReceivedCouponId,
    Integer discountCost,
    LocalDate expirationDate
) {

}
