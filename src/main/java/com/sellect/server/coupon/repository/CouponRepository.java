package com.sellect.server.coupon.repository;

import com.sellect.server.coupon.domain.Coupon;
import java.util.Optional;

public interface CouponRepository {
    void save(Coupon coupon);

    Optional<Coupon> findById(Long couponId);
}
