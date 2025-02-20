package com.sellect.server.cart.domain;

import com.sellect.server.auth.domain.User;
import com.sellect.server.product.domain.Product;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CartItem {

    private Long id;
    private User user;
    private Product product;
    private Integer quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deleteAt;

    // 현재 장바구니에 없을 때는 추가 / 있을 때는 +1
    public static CartItem add(User user, Product product, CartItem cartItem) {
        if (cartItem != null) {
            return cartItem.changeQuantity(1);
        }

        return CartItem.builder()
            .user(user)
            .product(product)
            .quantity(1)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .deleteAt(null)
            .build();
    }


    public CartItem changeQuantity(Integer quantity) {
        return CartItem.builder()
            .id(this.id)
            .user(this.user)
            .product(this.product)
            .quantity(this.quantity + quantity)
            .createdAt(this.createdAt)
            .updatedAt(LocalDateTime.now())
            .deleteAt(this.deleteAt)
            .build();
    }

    public CartItem remove() {
        return CartItem.builder()
            .id(this.id)
            .user(this.user)
            .product(this.product)
            .quantity(this.quantity) // 수량은 존재
            .createdAt(this.createdAt)
            .updatedAt(this.updatedAt)
            .deleteAt(LocalDateTime.now())
            .build();
    }

}