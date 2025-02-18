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

    /*
     * 쿠폰 등록기능
     * 쿠폰 수량 삭감 - 동시성 이슈 발생
     * ReentrantLock을 사용하여 해결 -> 애플리케이션에서 해결
     * 단일 인스턴스인 경우 가능한 부분
     * 스케일 아웃을 하면?? -> DB 락????
     * */
    @Transactional
    public void registerCoupon(User user, Long couponId) {
        // TODO: 애플리케이션 락 vs DB 락 vs 큐 성능측정 필요 2025-02-18, 17:7
//        lock.lock();
        Coupon coupon = couponRepository.findById(couponId)
            .orElseThrow(() -> new CommonException(BError.NOT_EXIST, String.valueOf(couponId)));
        if (coupon.getQuantity() <= 0) {
            throw new CommonException(BError.COUPON_QUANTITY_ZERO, couponId.toString());
        }

        try {

            coupon.decreaseQuantity();
            UserReceivedCoupon userReceivedCoupon = UserReceivedCoupon.create(user, coupon);
            userReceivedCouponRepository.save(userReceivedCoupon);
            couponRepository.save(coupon);
        } finally {
            lock.unlock();
        }
    }



    // TODO: 쿠폰 내역 조회(전체조회) 2025-02-18, 10:36
    public void getCoupon(User user) {
    }
}

