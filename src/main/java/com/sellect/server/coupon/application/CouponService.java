package com.sellect.server.coupon.application;

import com.sellect.server.auth.domain.User;
import com.sellect.server.auth.repository.entity.Role;
import com.sellect.server.common.exception.CommonException;
import com.sellect.server.common.exception.enums.BError;
import com.sellect.server.coupon.controller.request.IssueCouponRequest;
import com.sellect.server.coupon.controller.response.ActiveCouponResponse;
import com.sellect.server.coupon.controller.response.CouponInfo;
import com.sellect.server.coupon.controller.response.CouponPossibleOrderResponse;
import com.sellect.server.coupon.controller.response.CouponResponse;
import com.sellect.server.coupon.controller.response.SellerInfo;
import com.sellect.server.coupon.domain.Coupon;
import com.sellect.server.coupon.domain.UserReceivedCoupon;
import com.sellect.server.coupon.repository.CouponRepository;
import com.sellect.server.coupon.repository.UserReceivedCouponRepository;
import com.sellect.server.product.domain.Product;
import com.sellect.server.product.repository.ProductRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService {

    ReentrantLock lock = new ReentrantLock();
    private static final Sort DEFAULT_SORT = Sort.by(Direction.DESC, "createdAt");

    private final CouponRepository couponRepository;
    private final UserReceivedCouponRepository userReceivedCouponRepository;
    private final ProductRepository productRepository;

    public void uploadCoupon(User user, IssueCouponRequest issueCouponRequest) {
        if (user.getRole() != Role.SELLER) {
            throw new CommonException(BError.NOT_SELLER, user.getNickname());
        }
        Coupon coupon = Coupon.builder()
            .seller(user)
            .discountCost(issueCouponRequest.discount())
            .quantity(issueCouponRequest.quantity())
            .expirationDate(issueCouponRequest.expirationDate())
            .build();

        couponRepository.save(coupon);
    }

    /*
     * 쿠폰 등록기능
     * 쿠폰 수량 삭감 - 동시성 이슈 발생
     * ReentrantLock을 사용하여 해결 -> 애플리케이션에서 해결
     * 단일 인스턴스인 경우 가능한 부분
     * 스케일 아웃을 하면?? -> DB 락????
     * */

    // TODO: 애플리케이션 락 vs DB 락 vs 큐 성능측정 필요 2025-02-18, 17:7
    @Transactional
    public void downloadCoupon(User user, Long couponId) {
        lock.lock();
        try {
            Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CommonException(BError.NOT_EXIST, String.valueOf(couponId)));
            if (coupon.getQuantity() <= 0) {
                throw new CommonException(BError.COUPON_QUANTITY_ZERO, couponId.toString());
            }
            if (userReceivedCouponRepository.existsByUserAndCoupon(user, coupon)) {
                throw new CommonException(BError.ALREADY_RECEIVED, couponId.toString());
            }
            Coupon decreasedCoupon = coupon.decreaseQuantity();
            UserReceivedCoupon userReceivedCoupon = UserReceivedCoupon.create(user,
                decreasedCoupon);
            userReceivedCouponRepository.save(userReceivedCoupon);
            couponRepository.save(decreasedCoupon);
        } finally {
            lock.unlock();
        }
    }

    @Transactional(readOnly = true)
    public List<CouponResponse> getCouponList(User user, int page, int size, Boolean isUsed) {
        PageRequest pageRequest = PageRequest.of(page, size, DEFAULT_SORT);

        List<UserReceivedCoupon> receivedCoupons = (isUsed != null)
            ? userReceivedCouponRepository.findByUserAndIsUsed(user, pageRequest, isUsed)
            : userReceivedCouponRepository.findByUser(user, pageRequest);

        return receivedCoupons.stream()
            .map(this::toCouponResponse)
            .toList();
    }

    @Transactional
    public void useCoupon(User user, Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
            .orElseThrow(() -> new CommonException(BError.NOT_EXIST, String.valueOf(couponId)));
        UserReceivedCoupon userReceivedCoupon = userReceivedCouponRepository.findByUserAndCoupon(
                user, coupon)
            .orElseThrow(() -> new CommonException(BError.NOT_EXIST, String.valueOf(couponId)));
        UserReceivedCoupon usedCoupon = userReceivedCoupon.useCoupon();

        userReceivedCouponRepository.save(usedCoupon);
    }

    @Transactional(readOnly = true)
    public List<CouponPossibleOrderResponse> getCouponsByMatchingSeller(User user,
        List<Long> productIds) {
        // 1. productIds를 통해 판매자 리스트 가져오기
        List<User> sellers = productIds.stream()
            .map(productId -> productRepository.findById(productId)
                .orElseThrow(
                    () -> new CommonException(BError.NOT_EXIST, String.valueOf(productId))))
            .map(Product::getSeller)
            .toList();

        // 2. 사용하지 않은 쿠폰 중 유효기간이 남아 있는 것 필터링
        List<UserReceivedCoupon> validCoupons = userReceivedCouponRepository.findAllByUserAndIsUsed(
                user, false).stream()
            .filter(c -> c.getCoupon().getExpirationDate().isAfter(LocalDate.now().minusDays(1)))
            .toList();

        // 3. 판매자가 일치하는 쿠폰만 선택하여 변환
        return validCoupons.stream()
            .filter(c -> sellers.contains(c.getCoupon().getSeller()))
            .map(c -> new CouponPossibleOrderResponse(
                c.getId(),
                c.getCoupon().getDiscountCost(),
                c.getCoupon().getExpirationDate())
            ).toList();
    }

    @Transactional(readOnly = true)
    public Page<ActiveCouponResponse> getActiveCouponList(User user, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, DEFAULT_SORT);
        Page<Coupon> activeCoupons = couponRepository.findAllActiveCouponList(pageRequest);
        return activeCoupons.map(coupon -> createActiveCouponResponse(user, coupon));
    }

    private ActiveCouponResponse createActiveCouponResponse(User user, Coupon coupon) {
        boolean isRegistered = isCouponRegistered(user, coupon);
        CouponInfo couponInfo = toCouponInfo(coupon);
        return new ActiveCouponResponse(isRegistered, couponInfo);
    }

    private boolean isCouponRegistered(User user, Coupon coupon) {
        return user != null && userReceivedCouponRepository.existsByUserAndCoupon(user, coupon);
    }

    private CouponResponse toCouponResponse(UserReceivedCoupon coupon) {
        return new CouponResponse(coupon.getIsUsed(), toCouponInfo(coupon.getCoupon()));
    }

    private CouponInfo toCouponInfo(Coupon coupon) {
        return new CouponInfo(
            coupon.getId(),
            coupon.getDiscountCost(),
            coupon.getExpirationDate(),
            toSellerInfo(coupon.getSeller())
        );
    }

    private SellerInfo toSellerInfo(User user) {
        return new SellerInfo(user.getId(), user.getNickname());
    }
}

