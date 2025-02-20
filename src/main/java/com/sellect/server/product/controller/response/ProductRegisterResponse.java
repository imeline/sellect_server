package com.sellect.server.product.controller.response;

import com.sellect.server.product.domain.Product;
import lombok.Builder;

@Builder
public record ProductRegisterResponse(
    Long productId,
    String name
) {

    public static ProductRegisterResponse from(Product product) {
        return new ProductRegisterResponse(
            product.getId(),
            product.getName()
        );
    }

}
