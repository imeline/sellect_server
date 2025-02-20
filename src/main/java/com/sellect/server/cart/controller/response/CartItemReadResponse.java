package com.sellect.server.cart.controller.response;

import com.sellect.server.cart.domain.CartItem;
import com.sellect.server.product.domain.Product;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record CartItemReadResponse(
    Long cartItemId,
    Long productId,
    String brandName,
    String productName,
    BigDecimal productPrice,
    int quantity,
    String imageUrl

) {
    public static CartItemReadResponse from(CartItem cartItem, Product product, String imageUrl) {
        return CartItemReadResponse.builder()
            .cartItemId(cartItem.getId())
            .productId(product.getId())
            .brandName(product.getBrand().getName()) // todo: 여기서도 N+1 문제 발생 예상
            .productName(product.getName())
            .productPrice(product.getPrice())
            .quantity(cartItem.getQuantity())
            .imageUrl(imageUrl)
            .build();
    }

}
