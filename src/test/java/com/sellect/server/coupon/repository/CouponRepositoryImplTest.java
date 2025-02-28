package com.sellect.server.coupon.repository;


import static org.assertj.core.api.BDDAssertions.then;

import com.sellect.server.auth.domain.User;
import com.sellect.server.auth.repository.entity.Role;
import com.sellect.server.auth.repository.entity.UserEntity;
import com.sellect.server.coupon.domain.Coupon;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class CouponRepositoryImplTest {

    @Autowired
    EntityManager em;

    @Autowired
    private CouponJpaRepository couponJpaRepository;
    private CouponRepositoryImpl couponRepositoryImpl;
    UserEntity sellerEntity;

    @BeforeEach
    void setUp() {
        sellerEntity = UserEntity.from(User.register("seller", Role.SELLER));
        em.persist(sellerEntity);
        em.flush();
        couponRepositoryImpl = new CouponRepositoryImpl(couponJpaRepository);
    }

        @Test
        @DisplayName("쿠폰 저장 테스트")
        void couponSaveSuccess() {
            //given
            Coupon coupon = Coupon.builder()
                .id(1L)
                .seller(sellerEntity.toModel())
                .discountCost(1000)
                .quantity(10)
                .build();

            //when
            couponRepositoryImpl.save(coupon);

            //then
            Coupon savedCoupon = couponRepositoryImpl.findById(1L).orElse(null);
            then(savedCoupon.getId()).isEqualTo(1L);
            then(savedCoupon.getSeller().getNickname()).isEqualTo("seller");
            then(savedCoupon.getDiscountCost()).isEqualTo(1000);
            then(savedCoupon.getQuantity()).isEqualTo(10);
        }


}
