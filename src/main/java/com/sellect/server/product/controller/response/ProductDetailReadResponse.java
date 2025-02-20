package com.sellect.server.product.controller.response;

import com.sellect.server.auth.domain.User;
import com.sellect.server.brand.domain.Brand;
import com.sellect.server.category.domain.Category;
import com.sellect.server.product.domain.Product;
import com.sellect.server.product.domain.ProductImage;
import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;

@Builder
public record ProductDetailReadResponse(
    Long productId,
    String categoryName, // todo: 일단은 하나의 String 으로만 조회된다.
    String brandName,
    String sellerName,
    String name,
    BigDecimal price,
    String description,
    int stock,
    List<ProductImageResponse> images // 이미지 순서를 고려해서 이미지 전부 보내기
) {

    public static ProductDetailReadResponse from(Product product,
        Category category,
        User seller,
        Brand brand, List<ProductImage>productImages
        ) {
        return ProductDetailReadResponse.builder()
            .productId(product.getId())
            .categoryName(category.getName())
            .brandName(brand.getName())
            .sellerName(seller.getNickname())
            .name(product.getName())
            .price(product.getPrice())
            .description(product.getDescription())
            .stock(product.getStock())
            .images(productImages.stream()
                .map(ProductImageResponse::from)
                .toList())
            .build();
    }
}
