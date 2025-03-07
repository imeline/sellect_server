package com.sellect.server.coupon.repository;

import com.sellect.server.auth.domain.User;
import com.sellect.server.coupon.domain.Coupon;
import com.sellect.server.coupon.domain.UserReceivedCoupon;
import com.sellect.server.coupon.repository.entity.UserReceivedCouponEntity;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class FakeuserReceivedCouponRepository implements UserReceivedCouponRepository {

    Map<Long, UserReceivedCoupon> table = new HashMap<>();
    Long id = 1L;

    @Override
    public UserReceivedCouponEntity save(UserReceivedCoupon userReceivedCoupon) {
        if (userReceivedCoupon.getId() == null) {
            userReceivedCoupon = userReceivedCoupon.builder()
                .id(id++)
                .user(userReceivedCoupon.getUser())
                .coupon(userReceivedCoupon.getCoupon())
                .isUsed(userReceivedCoupon.getIsUsed())
                .createdAt(userReceivedCoupon.getCreatedAt())
                .updatedAt(userReceivedCoupon.getUpdatedAt())
                .deleteAt(userReceivedCoupon.getDeleteAt())
                .build();
        }
        table.put(userReceivedCoupon.getId(), userReceivedCoupon);
        return null;
    }

    @Override
    public List<UserReceivedCoupon> findByUser(User user, PageRequest pageRequest) {
        return table.values().stream()
            .filter(coupon -> coupon.getUser().getId().equals(user.getId()))
            .sorted(getComparator(pageRequest))
            .skip((long) pageRequest.getPageNumber() * pageRequest.getPageSize())
            .limit(pageRequest.getPageSize())
            .collect(Collectors.toList());
    }

    @Override
    public List<UserReceivedCoupon> findByUserAndIsUsed(User user, PageRequest pageRequest,
        Boolean isUsed) {
        return table.values().stream()
            .filter(coupon -> coupon.getUser().getId().equals(user.getId()))
            .filter(coupon -> coupon.getIsUsed().equals(isUsed))
            .sorted(getComparator(pageRequest))
            .skip((long) pageRequest.getPageNumber() * pageRequest.getPageSize())
            .limit(pageRequest.getPageSize())
            .collect(Collectors.toList());
    }

    @Override
    public List<UserReceivedCoupon> findAllByUserAndIsUsed(User user, boolean isUsed) {
        return table.values().stream()
            .filter(coupon -> coupon.getUser().getId().equals(user.getId()))
            .filter(coupon -> coupon.getIsUsed().equals(isUsed))
            .toList();
    }

    @Override
    public Optional<UserReceivedCoupon> findByUserAndCoupon(User user, Coupon coupon) {
        return Optional.empty();
    }

    @Override
    public Boolean existsByUserAndCoupon(User user, Coupon coupon) {
        return table.values().stream().anyMatch(
            coupon1 ->
                coupon1.getUser().getId().equals(user.getId()) &&
                    coupon1.getCoupon().getId()
                        .equals(coupon.getId()));
    }

    @Override
    public Optional<UserReceivedCoupon> findById(Long id) {
        return Optional.ofNullable(table.get(id));
    }

    private Comparator<UserReceivedCoupon> getComparator(Pageable pageable) {
        return pageable.getSort().stream()
            .map(order -> {
                Comparator<UserReceivedCoupon> comparator = Comparator.comparing(coupon -> {
                    switch (order.getProperty()) {
                        case "createdAt":
                            return coupon.getCreatedAt();
                        case "updatedAt":
                            return coupon.getUpdatedAt();
                        default:
                            throw new IllegalArgumentException(
                                "Unsupported sorting field: " + order.getProperty());
                    }
                });
                return order.isDescending() ? comparator.reversed() : comparator;
            })
            .reduce(Comparator::thenComparing)
            .orElse(Comparator.comparing(UserReceivedCoupon::getId));
    }

    public void clear() {
        table.clear();
    }

}
