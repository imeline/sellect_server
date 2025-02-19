package com.sellect.server.coupon.repository;

import com.sellect.server.auth.repository.entity.UserEntity;
import com.sellect.server.coupon.domain.Coupon;
import com.sellect.server.coupon.repository.entity.CouponEntity;
import com.sellect.server.coupon.repository.entity.UserReceivedCouponEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserReceivedCouponJpaRepository extends JpaRepository<UserReceivedCouponEntity, Long> {

    List<UserReceivedCouponEntity> findByUser(UserEntity user, Pageable pageable);

    List<UserReceivedCouponEntity> findByUserAndIsUsed(UserEntity from, PageRequest pageRequest, Boolean isUsed);

    Optional<UserReceivedCouponEntity> findByUserAndCoupon(UserEntity from, CouponEntity coupon);
    Boolean existsByUserAndCoupon(UserEntity user, CouponEntity coupon);
}
