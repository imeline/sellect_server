package com.sellect.server.search.controller.response;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record SearchResponse(
    String brandName,
    String productId,
    String imageUrl,
    String name,
    BigDecimal price

    // todo: 따로 빼야하지 않을까?
//    Integer reviewCount,
//    Long ratingAverage
) {

}
