package com.sellect.server.coupon.controller;

import com.sellect.server.auth.domain.User;
import com.sellect.server.common.infrastructure.annotation.AuthSeller;
import com.sellect.server.common.infrastructure.annotation.AuthUser;
import com.sellect.server.common.response.ApiResponse;
import com.sellect.server.coupon.application.CouponService;
import com.sellect.server.coupon.controller.request.IssueCouponRequest;
import com.sellect.server.coupon.controller.response.ActiveCouponResponse;
import com.sellect.server.coupon.controller.response.CouponResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/coupon")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/issue")
    public ApiResponse<?> issueCoupon(
        @AuthSeller User user,
        @RequestBody IssueCouponRequest issueCouponRequest
    ) {
        couponService.uploadCoupon(user, issueCouponRequest);
        return ApiResponse.ok(null);
    }

    @PutMapping("/register/{couponId}")
    public ApiResponse<?> registerCoupon(@AuthUser User user, @PathVariable Long couponId) {
        couponService.downloadCoupon(user, couponId);
        return ApiResponse.ok();
    }


    // reponse 데이터에 seller id, 이름 나오게 추가
    @GetMapping
    public ApiResponse<List<CouponResponse>> getCoupon(@AuthUser User user,
        @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size,
        @RequestParam(required = false) Boolean isUsed) {
        List<CouponResponse> couponList = couponService.getCouponList(user, page, size, isUsed);
        return ApiResponse.ok(couponList);
    }

    @GetMapping("/actives")
    public ApiResponse<Page<ActiveCouponResponse>> getActiveCouponList(
        @AuthUser User user,
        @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size
    ){
        Page<ActiveCouponResponse> activeCouponList = couponService.getActiveCouponList(user, page, size);
        return ApiResponse.ok(activeCouponList);
    }

}
