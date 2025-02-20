package com.sellect.server.cart.repository;

import com.sellect.server.cart.domain.CartItem;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository {

    CartItem save(CartItem cartItem);

    Optional<CartItem> findById(Long cartItemId);

    List<CartItem> findAllByUserId(Long userId);

    void saveAll(List<CartItem> cartItems);

    Optional<CartItem> findByProductId(Long productId);
}
