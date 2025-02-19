package com.sellect.server.coupon.controller.response;

public record CouponResponse(
    Boolean isUsed,
    CouponInfo couponInfo

) {

}
