package com.sellect.server.coupon.repository;

import com.sellect.server.coupon.domain.UserReceivedCoupon;
import com.sellect.server.coupon.repository.entity.UserReceivedCouponEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserReceivedCouponRepositoryImpl implements UserReceivedCouponRepository {

    private final UserReceivedCouponJpaRepository userReceivedCouponJpaRepository;

    public void save(UserReceivedCoupon userReceivedCoupon) {
        userReceivedCouponJpaRepository.save(UserReceivedCouponEntity.from(userReceivedCoupon));
    }

}
