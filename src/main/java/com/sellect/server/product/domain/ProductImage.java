package com.sellect.server.product.domain;

import com.sellect.server.product.controller.request.ImageContextCreateRequest;
import com.sellect.server.product.controller.request.ImageContextUpdateRequest;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductImage {

    private final Long id;
    private final Product product;
    private final String imageUrl;
    private final boolean representative;
    private final Integer sequence;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deleteAt;

    public static ProductImage register(Product product, String imageUrl, ImageContextCreateRequest request) {
        return ProductImage.builder()
            .product(product)
            .imageUrl(imageUrl)
            .representative(request.isRepresentative())
            .sequence(request.sequence())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    public static ProductImage register(Product product, String imageUrl, ImageContextUpdateRequest request) {
        return ProductImage.builder()
            .product(product)
            .imageUrl(imageUrl)
            .representative(request.isRepresentative())
            .sequence(request.sequence())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    public ProductImage update(ImageContextUpdateRequest request) {
        return ProductImage.builder()
            .id(this.id)
            .product(this.product)
            .imageUrl(this.imageUrl)
            .representative(request.isRepresentative()) // 대표 이미지 여부 업데이트
            .sequence(request.sequence()) // 순서 업데이트
            .createdAt(this.getCreatedAt())
            .updatedAt(LocalDateTime.now())
            .deleteAt(this.deleteAt)
            .build();
    }

    public ProductImage updateImageUrl(String imageUrl) {
        return ProductImage.builder()
            .id(this.id)
            .product(this.product)
            .imageUrl(imageUrl)
            .representative(this.representative)
            .sequence(this.sequence)
            .createdAt(this.getCreatedAt())
            .updatedAt(LocalDateTime.now())
            .deleteAt(this.deleteAt)
            .build();
    }

    public ProductImage remove() {
        return ProductImage.builder()
            .id(this.id)
            .product(this.product)
            .imageUrl(this.imageUrl)
            .representative(this.representative)
            .sequence(this.sequence)
            .createdAt(this.getCreatedAt())
            .updatedAt(this.getUpdatedAt())
            .deleteAt(LocalDateTime.now()) // 삭제 시간 업데이트
            .build();
    }
}
