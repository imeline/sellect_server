package com.sellect.server.cart.controller.response;

import com.sellect.server.cart.domain.CartItem;
import lombok.Builder;

@Builder
public record CartItemQuantityChangeResponse(
    Long id,
    Integer quantity
) {

    public static CartItemQuantityChangeResponse from(CartItem cartItem) {
        return  CartItemQuantityChangeResponse.builder()
            .id(cartItem.getId())
            .quantity(cartItem.getQuantity())
            .build();
    }
}
