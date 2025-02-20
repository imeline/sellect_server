package com.sellect.server.brand.controller.response;

import com.sellect.server.brand.domain.Brand;
import lombok.Builder;

@Builder
public record BrandReadResponse(
    Long id,
    String name

) {
    public static BrandReadResponse from(Brand brand) {
        return BrandReadResponse.builder()
            .id(brand.getId())
            .name(brand.getName())
            .build();
    }
}