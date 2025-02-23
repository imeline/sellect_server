package com.sellect.server.product.repository;

import com.sellect.server.product.domain.Product;
import com.sellect.server.product.domain.ProductImage;
import java.util.List;
import java.util.Optional;

public interface ProductImageRepository {

    void save(ProductImage productImage, Product product);

    Optional<ProductImage> findByProductImageId(Long productId);

    List<ProductImage> findByProductId(Long productId);

    // Optional 일 수 없음 정책 상
    ProductImage findByThumbnailImage(Long productId);

}
