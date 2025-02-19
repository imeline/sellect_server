package com.sellect.server.coupon.repository;

import com.sellect.server.auth.domain.User;
import com.sellect.server.coupon.domain.Coupon;
import com.sellect.server.coupon.domain.UserReceivedCoupon;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;

public interface UserReceivedCouponRepository {

    void save(UserReceivedCoupon userReceivedCoupon);

    List<UserReceivedCoupon> findByUser(User user, PageRequest pageRequest);

    List<UserReceivedCoupon> findByUserAndIsUsed(User user, PageRequest pageRequest,
        Boolean isUsed);

    Optional<UserReceivedCoupon> findByUserAndCoupon(User user, Coupon coupon);

    Boolean existsByUserAndCoupon(User user, Coupon coupon);

    Optional<UserReceivedCoupon> findById(Long id);
}
