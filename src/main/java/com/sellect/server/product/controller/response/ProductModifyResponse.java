package com.sellect.server.product.controller.response;

import com.sellect.server.product.domain.Product;
import java.math.BigDecimal;

public record ProductModifyResponse(
    String name,
    BigDecimal price,
    String description,
    Integer stock

) {

    public static ProductModifyResponse from(Product product) {
        return new ProductModifyResponse(
            product.getName(),
            product.getPrice(),
            product.getDescription(),
            product.getStock()
        );
    }
}
