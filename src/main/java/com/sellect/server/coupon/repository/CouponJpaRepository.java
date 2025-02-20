package com.sellect.server.coupon.repository;

import com.sellect.server.coupon.repository.entity.CouponEntity;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponJpaRepository extends JpaRepository<CouponEntity, Long> {

    Page<CouponEntity> findByCreatedAtNullAndQuantityGreaterThanAndExpirationDateAfter(
        Integer quantity, LocalDate expirationDate, Pageable pageable);

    Page<CouponEntity> findByDeleteAtNullAndQuantityGreaterThanAndExpirationDateAfter(
        Integer quantity, LocalDate expirationDate, Pageable pageable);
}
