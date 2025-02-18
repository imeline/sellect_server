package com.sellect.server.coupon.controller.request;

import java.time.LocalDate;

public record IssueCouponRequest(int quantity, int discount, LocalDate expirationDate) {

}