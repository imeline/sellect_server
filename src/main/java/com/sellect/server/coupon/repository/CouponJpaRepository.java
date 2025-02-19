package com.sellect.server.coupon.repository;

import com.sellect.server.coupon.repository.entity.CouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponJpaRepository extends JpaRepository<CouponEntity, Long> {
}
