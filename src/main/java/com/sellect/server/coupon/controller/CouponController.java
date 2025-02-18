package com.sellect.server.coupon.controller;

import com.sellect.server.auth.domain.User;
import com.sellect.server.common.infrastructure.annotation.AuthSeller;
import com.sellect.server.common.infrastructure.annotation.AuthUser;
import com.sellect.server.common.response.ApiResponse;
import com.sellect.server.coupon.application.CouponService;
import com.sellect.server.coupon.controller.request.IssueCouponRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coupon")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    // TODO: 쿠폰 발급 API
    /*
     * 판매자가 쿠폰을 발급하는 API
     *
     * */
    @PostMapping("/issue")
    public ApiResponse<?> issueCoupon(
        @AuthSeller User user,
        @RequestBody IssueCouponRequest issueCouponRequest
    ) {
        couponService.issueCoupon(user, issueCouponRequest);
        return ApiResponse.ok(null);
    }


}
