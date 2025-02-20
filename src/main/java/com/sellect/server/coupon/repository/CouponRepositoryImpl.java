package com.sellect.server.coupon.repository;

import com.sellect.server.coupon.domain.Coupon;
import com.sellect.server.coupon.repository.entity.CouponEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    @Override
    public Page<Coupon> findAllActiveCouponList(PageRequest request) {
        Page<CouponEntity> activeCouponList = couponJpaRepository.findByDeleteAtNullAndQuantityGreaterThanAndExpirationDateAfter(
            0,
            LocalDate.now(), request);
        return activeCouponList.map(CouponEntity::toModel);
    }
}
