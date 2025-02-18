package com.sellect.server.coupon.repository;

import com.sellect.server.auth.domain.User;
import com.sellect.server.auth.repository.entity.UserEntity;
import com.sellect.server.coupon.domain.UserReceivedCoupon;
import com.sellect.server.coupon.repository.entity.UserReceivedCouponEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserReceivedCouponRepositoryImpl implements UserReceivedCouponRepository {

    private final UserReceivedCouponJpaRepository userReceivedCouponJpaRepository;

    public void save(UserReceivedCoupon userReceivedCoupon) {
        userReceivedCouponJpaRepository.save(UserReceivedCouponEntity.from(userReceivedCoupon));
    }

    @Override
    public List<UserReceivedCoupon> findByUser(User user, PageRequest pageRequest) {
        List<UserReceivedCouponEntity> receivedCouponEntityList = userReceivedCouponJpaRepository.findByUser(
            UserEntity.from(user), pageRequest);

        return receivedCouponEntityList.stream()
            .map(UserReceivedCouponEntity::toModel)
            .toList();
    }

    @Override
    public List<UserReceivedCoupon> findByUserAndIsUsed(User user, PageRequest pageRequest,
        Boolean isUsed) {
        List<UserReceivedCouponEntity> receivedCouponEntityList = userReceivedCouponJpaRepository.findByUserAndIsUsed(
            UserEntity.from(user), pageRequest, isUsed);

        return receivedCouponEntityList.stream()
            .map(UserReceivedCouponEntity::toModel)
            .toList();
    }

}
