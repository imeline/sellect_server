package com.sellect.server.coupon.repository;

import com.sellect.server.coupon.domain.Coupon;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public class FakeCouponRepository implements CouponRepository {

    //        private final Map<Long, Coupon> storage = new HashMap<>();
    private final Map<Long, Coupon> storage = new ConcurrentHashMap<>();
    private Long id = 1L;

    @Override
    public Coupon save(Coupon coupon) {
            if (coupon.getId() == null) {
                coupon = Coupon.builder()
                    .id(id++)
                    .seller(coupon.getSeller())
                    .discountCost(coupon.getDiscountCost())
                    .quantity(coupon.getQuantity())
                    .expirationDate(coupon.getExpirationDate())
                    .createdAt(coupon.getCreatedAt())
                    .updatedAt(coupon.getUpdatedAt())
                    .deleteAt(coupon.getDeleteAt())
                    .build();
            }
            storage.put(coupon.getId(), coupon);
        return coupon;
    }

    @Override
    public Optional<Coupon> findById(Long couponId) {
        return Optional.ofNullable(storage.get(couponId));
    }

    @Override
    public Page<Coupon> findAllActiveCouponList(PageRequest request) {
        return Page.empty();
    }
}
