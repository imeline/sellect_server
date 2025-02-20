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
    public List<Coupon> findAllActiveCouponList(PageRequest request) {
        Page<CouponEntity> activeCouponList = couponJpaRepository.findByCreatedAtNullAndQuantityGreaterThanAndExpirationDateAfter(
            0,
            LocalDate.now(), request);
        List<CouponEntity> content = activeCouponList.getContent();

        return content.stream()
            .map(CouponEntity::toModel)
            .toList();
    }
}
