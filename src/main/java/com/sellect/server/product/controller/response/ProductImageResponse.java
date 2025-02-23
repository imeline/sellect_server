package com.sellect.server.product.controller.response;

import com.sellect.server.product.domain.ProductImage;
import lombok.Builder;

@Builder
public record ProductImageResponse(
    Long productImageId,
    String imageUrl,
    Integer sequence,
    boolean representative
) {
    public static ProductImageResponse from(ProductImage productImage) {
        return ProductImageResponse.builder()
            .productImageId(productImage.getId())
            .imageUrl(productImage.getImageUrl())
            .sequence(productImage.getSequence())
            .representative(productImage.isRepresentative())
            .build();

    }
}
