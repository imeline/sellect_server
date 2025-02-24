package com.sellect.server.product.controller.response;

import com.sellect.server.category.domain.Category;
import com.sellect.server.product.domain.Product;
import com.sellect.server.product.domain.ProductImage;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record ProductDetailRetrieveBySellerResponse(
    Long productId,
    String name,
    BigDecimal price,
    Integer stock,
    String description,
    String smallCategoryName,
    String mediumCategoryName,
    String largeCategoryName,
    String brandName,
    List<ProductImageResponse> images,
    Integer totalOrders,
    BigDecimal totalSales,
    LocalDateTime createdAt
) {

    public static ProductDetailRetrieveBySellerResponse from(
        Product product,
        List<ProductImage> productImages,
        Category smallCategory,
        Category mediumCategory,
        Category largeCategory,
        Integer totalOrders,
        BigDecimal totalSales) {
        return ProductDetailRetrieveBySellerResponse.builder()
            .productId(product.getId())
            .name(product.getName())
            .price(product.getPrice())
            .stock(product.getStock())
            .description(product.getDescription())
            .smallCategoryName(smallCategory.getName())
            .mediumCategoryName(mediumCategory.getName())
            .largeCategoryName(largeCategory.getName())
            .brandName(product.getBrand().getName())
            .images(productImages.stream()
                .map(ProductImageResponse::from)
                .toList())
            .totalOrders(totalOrders)
            .totalSales(totalSales)
            .createdAt(product.getCreatedAt())
            .build();
    }

}
