package com.sellect.server.coupon.repository;

import com.sellect.server.coupon.domain.Coupon;
import com.sellect.server.coupon.repository.entity.CouponEntity;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepository {

    private final CouponJpaRepository couponJpaRepository;

    @Override
    public void save(Coupon coupon) {
        couponJpaRepository.save(CouponEntity.from(coupon));
    }

    @Override
    public Optional<Coupon> findById(Long couponId) {
        return Optional.empty();
    }
}
