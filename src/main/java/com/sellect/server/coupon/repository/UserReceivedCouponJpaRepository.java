package com.sellect.server.coupon.repository;

import com.sellect.server.coupon.repository.entity.UserReceivedCouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserReceivedCouponJpaRepository extends JpaRepository<UserReceivedCouponEntity, Long> {

}
