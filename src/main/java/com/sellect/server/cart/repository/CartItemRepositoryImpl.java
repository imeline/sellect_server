package com.sellect.server.cart.repository;

import com.sellect.server.cart.domain.CartItem;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CartItemRepositoryImpl implements CartItemRepository {

    private final CartItemJpaRepository cartItemJpaRepository;

    @Override
    public CartItem save(CartItem cartItem) {
        return cartItemJpaRepository.save(CartItemEntity.from(cartItem)).toModel();
    }

    @Override
    public Optional<CartItem> findById(Long cartItemId) {
        return cartItemJpaRepository.findById(cartItemId)
            .map(CartItemEntity::toModel);
    }

    @Override
    public List<CartItem> findAllByUserId(Long userId) {
        return cartItemJpaRepository.findByUserId(userId).stream()
            .map(CartItemEntity::toModel)
            .toList();
    }

    @Override
    public void saveAll(List<CartItem> cartItems) {
        cartItemJpaRepository.saveAll(cartItems.stream()
            .map(CartItemEntity::from)
            .toList());
    }

    @Override
    public Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId) {
        return cartItemJpaRepository.findByUserEntityIdAndProductEntityIdAndDeleteAtIsNull(userId, productId)
            .map(CartItemEntity::toModel);
    }

    @Override
    public Long countByUserId(Long userId) {
        return cartItemJpaRepository.countByUserEntityIdAndDeleteAtIsNull(userId);
    }
}
