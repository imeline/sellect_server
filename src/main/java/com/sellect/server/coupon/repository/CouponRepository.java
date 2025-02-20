package com.sellect.server.coupon.repository;

import com.sellect.server.coupon.domain.Coupon;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;

public interface CouponRepository {
    void save(Coupon coupon);

    Optional<Coupon> findById(Long couponId);

    List<Coupon> findAllActiveCouponList(PageRequest request);


}
