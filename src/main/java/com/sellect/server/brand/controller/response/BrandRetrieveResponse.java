package com.sellect.server.brand.controller.response;

import com.sellect.server.brand.domain.Brand;
import lombok.Builder;

@Builder
public record BrandRetrieveResponse(
    Long id,
    String name

) {
    public static BrandRetrieveResponse from(Brand brand) {
        return BrandRetrieveResponse.builder()
            .id(brand.getId())
            .name(brand.getName())
            .build();
    }
}