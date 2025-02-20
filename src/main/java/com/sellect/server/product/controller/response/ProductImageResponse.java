package com.sellect.server.product.controller.response;

import com.sellect.server.product.domain.ProductImage;
import lombok.Builder;

@Builder
public record ProductImageResponse(
    Long id,
    String imageUrl,
    boolean representative,
    String uuid,
    String prev,
    String next
) {
    public static ProductImageResponse from(ProductImage productImage) {
        return ProductImageResponse.builder()
            .id(productImage.getId())
            .imageUrl(productImage.getImageUrl())
            .representative(productImage.isRepresentative())
            .uuid(productImage.getUuid())
            .prev(productImage.getPrev())
            .next(productImage.getNext())
            .build();

    }
}
