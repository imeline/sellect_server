package com.sellect.server.cart.repository;

import com.sellect.server.cart.domain.CartItem;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FakeCartItemRepository implements CartItemRepository {

    private final Map<Long, CartItem> data = new HashMap<>();

    @Override
    public CartItem save(CartItem cartItem) {
        if (cartItem.getId() == null) {
            // 새로운 ID 자동 생성 (현재 데이터 개수 + 1)
            long newId = data.size() + 1;
            cartItem = CartItem.builder()
                .id(newId)
                .user(cartItem.getUser())
                .product(cartItem.getProduct())
                .quantity(cartItem.getQuantity())
                .createdAt(cartItem.getCreatedAt() != null ? cartItem.getCreatedAt() : LocalDateTime.now())
                .updatedAt(LocalDateTime.now()) // 저장 시점 업데이트
                .deleteAt(cartItem.getDeleteAt())
                .build();
        } else {
            // 기존 데이터가 있으면 업데이트 (Map은 put()으로 덮어쓰기 가능)
            data.put(cartItem.getId(), cartItem);
        }

        return cartItem;
    }

    @Override
    public Optional<CartItem> findById(Long id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public List<CartItem> findAllByUserId(Long userId) {
        List<CartItem> result = new ArrayList<>();
        for (CartItem item : data.values()) {
            if (item.getUser().getId().equals(userId)) {
                result.add(item);
            }
        }
        return result;
    }

    @Override
    public void saveAll(List<CartItem> cartItems) {
        for (CartItem item : cartItems) {
            data.put(item.getId(), item);
        }
    }

    // todo: MVP 개발 후에 테스트 코드 작성 시 구현
    @Override
    public Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId) {
        throw new RuntimeException("아직 fakeRepository 구현하지 않음");
    }

    @Override
    public Long countByUserId(Long userId) {
        // TODO: implement this method
        return 0L;
    }

    public void clear() {
        data.clear();
    }
}
