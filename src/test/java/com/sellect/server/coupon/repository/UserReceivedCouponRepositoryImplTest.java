package com.sellect.server.coupon.repository;

import static org.assertj.core.api.BDDAssertions.then;

import com.sellect.server.auth.domain.User;
import com.sellect.server.auth.repository.entity.Role;
import com.sellect.server.auth.repository.entity.UserEntity;
import com.sellect.server.coupon.domain.Coupon;
import com.sellect.server.coupon.domain.UserReceivedCoupon;
import com.sellect.server.coupon.repository.entity.CouponEntity;
import com.sellect.server.coupon.repository.entity.UserReceivedCouponEntity;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
class UserReceivedCouponRepositoryImplTest {

    @Autowired
    EntityManager em;

    @Autowired
    private UserReceivedCouponJpaRepository userReceivedCouponJpaRepository;
    private UserReceivedCouponRepositoryImpl userReceivedCouponRepository;


    User user;
    UserEntity sellerEntity;
    UserEntity userEntity;
    CouponEntity couponEntity1;
    CouponEntity couponEntity2;
    LocalDateTime firstCouponDate = LocalDateTime.now();
    LocalDateTime secondCouponDate = LocalDateTime.now().plusDays(2);

    @BeforeEach
    void setUp() {

        user = User.register("user", Role.USER);
        userReceivedCouponRepository = new UserReceivedCouponRepositoryImpl(
            userReceivedCouponJpaRepository);
        sellerEntity = UserEntity.from(User.register("seller", Role.SELLER));
        userEntity = UserEntity.from(user);

        em.persist(sellerEntity);
        em.persist(userEntity);

        couponEntity1 = CouponEntity.from(Coupon.builder()
            .seller(sellerEntity.toModel())
            .discountCost(1000)
            .quantity(10)
            .expirationDate(LocalDate.now().plusDays(30))
            .createdAt(firstCouponDate)
            .updatedAt(firstCouponDate)
            .build());

        couponEntity2 = CouponEntity.from(Coupon.builder()
            .seller(sellerEntity.toModel())
            .discountCost(1000)
            .quantity(10)
            .expirationDate(LocalDate.now().plusDays(30))
            .createdAt(secondCouponDate)
            .updatedAt(secondCouponDate)
            .build());

        em.persist(couponEntity1);
        em.persist(couponEntity2);
        em.flush();
        user = userEntity.toModel();
    }


    @Nested
    @DisplayName("save() ")
    class UserReceivedCouponSaveTest {

        @Test
        @DisplayName("[성공]")
        void willSuccess() {
            //given
            UserReceivedCoupon userReceivedCoupon = UserReceivedCoupon.create(user,
                couponEntity1.toModel());

            //when
            userReceivedCouponRepository.save(userReceivedCoupon);

            //then
            List<UserReceivedCouponEntity> allByUser = userReceivedCouponJpaRepository.findAllByUser(
                userEntity, PageRequest.of(0, 5));
            then(allByUser.size()).isEqualTo(1);
        }
    }


    @Nested
    @DisplayName("findByUser() ")
    class UserReceivedCouponFindByUserTest {

        @Test
        @DisplayName("[성공]")
        void willSuccess() {
            // given
            UserReceivedCoupon userReceivedCoupon1 = UserReceivedCoupon.create(user, couponEntity1.toModel());
            UserReceivedCoupon userReceivedCoupon2 = UserReceivedCoupon.create(user, couponEntity2.toModel());
            userReceivedCouponRepository.save(userReceivedCoupon1);
            userReceivedCouponRepository.save(userReceivedCoupon2);

            // when
            List<UserReceivedCoupon> byUser = userReceivedCouponRepository.findByUser(user, PageRequest.of(0, 5));

            // then
            then(byUser).isNotNull();
            then(byUser.size()).isEqualTo(2);
            then(byUser).extracting("coupon.id").containsExactlyInAnyOrder(couponEntity1.getId(), couponEntity2.getId());
            then(byUser).allMatch(urc -> urc.getUser().getId().equals(user.getId()));
        }
    }

    @Nested
    @DisplayName("findAllByUserAndIsUsed() ")
    class UserReceivedCouponFindAllByUserAndIsUsedTest {

        @Test
        @DisplayName("[성공]")
        void willSuccess() {
            // given
            UserReceivedCoupon userReceivedCoupon1 = UserReceivedCoupon.create(user, couponEntity1.toModel());
            UserReceivedCoupon userReceivedCoupon2 = UserReceivedCoupon.create(user, couponEntity2.toModel());
            UserReceivedCoupon usedCoupon1 = userReceivedCoupon1.useCoupon();// 사용 처리
            UserReceivedCoupon usedCoupon2 = userReceivedCoupon2.useCoupon();// 사용 처리
            userReceivedCouponRepository.save(usedCoupon1);
            userReceivedCouponRepository.save(usedCoupon2);

            // when
            List<UserReceivedCoupon> usedCoupons = userReceivedCouponRepository.findAllByUserAndIsUsed(user, true);

            // then
            then(usedCoupons).isNotNull();
            then(usedCoupons.size()).isEqualTo(2);
            then(usedCoupons.get(0).getCoupon().getId()).isEqualTo(couponEntity1.getId());
            then(usedCoupons.get(0).getIsUsed()).isTrue();
        }
    }

    @Nested
    @DisplayName("findByUserAndIsUsed() ")
    class UserReceivedCouponFindByUserAndIsUsedTest {

        @Test
        @DisplayName("[성공]")
        void willSuccess() {
            // given
            UserReceivedCoupon userReceivedCoupon1 = UserReceivedCoupon.create(user, couponEntity1.toModel());
            UserReceivedCoupon userReceivedCoupon2 = UserReceivedCoupon.create(user, couponEntity2.toModel());
            UserReceivedCoupon usedReceivedCoupon = userReceivedCoupon1.useCoupon();// 사용 처리
            userReceivedCouponRepository.save(usedReceivedCoupon);
            userReceivedCouponRepository.save(userReceivedCoupon2);

            // when
            List<UserReceivedCoupon> usedCoupons = userReceivedCouponRepository.findByUserAndIsUsed(user, PageRequest.of(0, 5), true);

            // then
            then(usedCoupons).isNotNull();
            then(usedCoupons.size()).isEqualTo(1);
            then(usedCoupons.get(0).getCoupon().getId()).isEqualTo(couponEntity1.getId());
            then(usedCoupons.get(0).getIsUsed()).isTrue();
        }
    }

    @Nested
    @DisplayName("findByUserAndCoupon() ")
    class UserReceivedCouponFindByUserAndCouponTest {

        @Test
        @DisplayName("[성공]")
        void willSuccess() {
            // given
            UserReceivedCoupon userReceivedCoupon = UserReceivedCoupon.create(user, couponEntity1.toModel());
            userReceivedCouponRepository.save(userReceivedCoupon);

            // when
            Optional<UserReceivedCoupon> foundCoupon = userReceivedCouponRepository.findByUserAndCoupon(user, couponEntity1.toModel());

            // then
            then(foundCoupon).isPresent();
            then(foundCoupon.get().getUser().getId()).isEqualTo(user.getId());
            then(foundCoupon.get().getCoupon().getId()).isEqualTo(couponEntity1.getId());
        }
    }

    @Nested
    @DisplayName("existsByUserAndCoupon() ")
    class UserReceivedCouponExistByUserAndCouponTest {

        @Test
        @DisplayName("[성공]")
        void willSuccess() {
            // given
            UserReceivedCoupon userReceivedCoupon = UserReceivedCoupon.create(user, couponEntity1.toModel());
            userReceivedCouponRepository.save(userReceivedCoupon);

            // when
            Boolean exists = userReceivedCouponRepository.existsByUserAndCoupon(user, couponEntity1.toModel());

            // then
            then(exists).isTrue();
        }
    }

    @Nested
    @DisplayName("findById() ")
    class UserReceivedCouponFindByIdTest {

        @Test
        @DisplayName("[성공]")
        void willSuccess() {
            // given
            UserReceivedCoupon userReceivedCoupon = UserReceivedCoupon.create(user, couponEntity1.toModel());
            UserReceivedCouponEntity savedCoupon = userReceivedCouponRepository.save(userReceivedCoupon);
            Long id = userReceivedCouponJpaRepository.findAll().get(0).getId(); // 저장된 ID 가져오기

            // when
            Optional<UserReceivedCoupon> foundCoupon = userReceivedCouponRepository.findById(id);

            // then
            then(foundCoupon).isPresent();
            then(foundCoupon.get().getUser().getId()).isEqualTo(user.getId());
            then(foundCoupon.get().getCoupon().getId()).isEqualTo(couponEntity1.getId());
        }
    }
}