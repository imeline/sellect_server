package com.sellect.server.product.controller.response;

import com.sellect.server.product.domain.Product;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record ProductModifyResponse(
    Long productId,
    String name,
    BigDecimal price,
    String description,
    Integer stock

) {

    public static ProductModifyResponse from(Product product) {
        return ProductModifyResponse.builder()
            .productId(product.getId())
            .name(product.getName())
            .price(product.getPrice())
            .description(product.getDescription())
            .stock(product.getStock())
            .build();
    }
}
