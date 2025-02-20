package com.sellect.server.cart.controller.response;

import com.sellect.server.cart.domain.CartItem;
import lombok.Builder;

@Builder
public record CardAddItemResponse(
    Long id,
    Long productId,
    Integer quantity
) {

    public static CardAddItemResponse from(CartItem cartItem) {
        return CardAddItemResponse.builder()
            .id(cartItem.getId())
            .productId(cartItem.getProduct().getId())
            .quantity(cartItem.getQuantity())
            .build();
    }
}
