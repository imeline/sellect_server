package com.sellect.server.coupon.application;

import static org.junit.jupiter.api.Assertions.*;

import com.sellect.server.auth.domain.User;
import com.sellect.server.auth.repository.entity.Role;
import com.sellect.server.common.exception.CommonException;
import com.sellect.server.coupon.controller.request.IssueCouponRequest;
import com.sellect.server.coupon.domain.Coupon;
import com.sellect.server.coupon.repository.CouponRepository;
import com.sellect.server.coupon.repository.FakeCouponRepository;
import com.sellect.server.coupon.repository.FakeuserReceivedCouponRepository;
import com.sellect.server.coupon.repository.UserReceivedCouponRepository;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CouponServiceTest {

    CouponService couponService;
    CouponRepository couponRepository;
    UserReceivedCouponRepository userReceivedCouponRepository;

    @BeforeEach
    void setUp() {
        couponRepository = new FakeCouponRepository();
        userReceivedCouponRepository = new FakeuserReceivedCouponRepository();
        couponService = new CouponService(couponRepository, userReceivedCouponRepository);
    }


    @Nested
    @DisplayName("issueCoupon() 메소드는")
    class issueCouponTest {

        @Test
        @DisplayName("유저가 판매자가 아닐때 exception을 던진다.")
        void userIsNotSellerThrowsException() {
            //given
            User user = User.builder()
                .id(1L)
                .nickname("test")
                .uuid("uuid")
                .role(Role.USER)
                .build();
            //when
            Exception exception = assertThrows(CommonException.class, () -> {
                couponService.issueCoupon(user,
                    new IssueCouponRequest(1, 10, LocalDate.now().plusDays(10)));
            });

            //then
            assertEquals("test is not a seller", exception.getMessage());
        }

        @Test
        @DisplayName("쿠폰을 정상적으로 등록한다")
        void _willSuccess() {
            //given
            User user = User.builder()
                .id(1L)
                .nickname("test")
                .uuid("uuid")
                .role(Role.SELLER)
                .build();

            IssueCouponRequest request = new IssueCouponRequest(10, 3000,
                LocalDate.now().plusDays(10));

            //when
            couponService.issueCoupon(user, request);

            //then
            assertEquals(10, couponRepository.findById(1L).get().getQuantity());
            assertEquals(3000, couponRepository.findById(1L).get().getDiscountCost());
        }
    }



}