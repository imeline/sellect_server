package com.sellect.server.product.controller.response;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record SellerStatsRetrieveResponse(
    BigDecimal totalSales,
    Integer totalProductsCount
) {

    public static SellerStatsRetrieveResponse from(BigDecimal totalSales, Integer totalProductsCount) {
        return SellerStatsRetrieveResponse.builder()
            .totalSales(totalSales)
            .totalProductsCount(totalProductsCount)
            .build();
    }

}
