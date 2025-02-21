package com.sellect.server.coupon.controller.response;

import java.time.LocalDate;

public record CouponInfo(
    Long couponId,
    Integer discountCost,
    LocalDate expirationDate,
    SellerInfo sellerInfo
) {

}
