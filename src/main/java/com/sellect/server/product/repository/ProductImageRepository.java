package com.sellect.server.product.repository;

import com.sellect.server.product.domain.Product;
import com.sellect.server.product.domain.ProductImage;
import java.util.List;
import java.util.Optional;

public interface ProductImageRepository {

    void save(ProductImage productImage, Product product);

    // todo: uuid 는 받는 이유가 궁금합니다.
    Optional<ProductImage> findByProductIdAndUuid(Long productId, String uuid);

    List<ProductImage> findByProductId(Long productId);

}
