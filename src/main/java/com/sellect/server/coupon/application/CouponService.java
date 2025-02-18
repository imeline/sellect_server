package com.sellect.server.coupon.application;

import com.sellect.server.auth.domain.User;
import com.sellect.server.auth.repository.entity.Role;
import com.sellect.server.common.exception.CommonException;
import com.sellect.server.common.exception.enums.BError;
import com.sellect.server.coupon.controller.request.IssueCouponRequest;
import com.sellect.server.coupon.domain.Coupon;
import com.sellect.server.coupon.domain.UserReceivedCoupon;
import com.sellect.server.coupon.repository.CouponRepository;
import com.sellect.server.coupon.repository.UserReceivedCouponRepository;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService {
    ReentrantLock lock = new ReentrantLock();

    // TODO: 쿠폰발급 2025-02-18, 10:36
    private final CouponRepository couponRepository;
    private final UserReceivedCouponRepository userReceivedCouponRepository;

    public void issueCoupon(User user, IssueCouponRequest issueCouponRequest) {
        if (user.getRole() != Role.SELLER) {
            throw new CommonException(BError.NOT_SELLER , user.getNickname());
        }
        Coupon coupon = Coupon.builder()
            .seller(user)
            .discountCost(issueCouponRequest.discount())
            .quantity(issueCouponRequest.quantity())
            .expirationDate(issueCouponRequest.expirationDate())
            .build();

        couponRepository.save(coupon);
    }





    // TODO: 쿠폰 내역 조회(전체조회) 2025-02-18, 10:36
    public void getCoupon(User user) {
    }
}

