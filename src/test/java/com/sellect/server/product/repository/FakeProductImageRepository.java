package com.sellect.server.product.repository;

import com.sellect.server.product.domain.Product;
import com.sellect.server.product.domain.ProductImage;

import java.util.*;
import org.springframework.data.util.Pair;

public class FakeProductImageRepository implements ProductImageRepository {

    private final Map<Long, Pair<ProductImage, Product>> storage = new HashMap<>();

    @Override
    public void save(ProductImage productImage, Product product) {
        storage.put(productImage.getId(), Pair.of(productImage, product));
    }

    @Override
    public Optional<ProductImage> findByProductImageId(Long productImageId) {
        return Optional.ofNullable(storage.get(productImageId))
            .filter(pair -> pair.getFirst().getDeleteAt() == null)
            .map(Pair::getFirst);
    }

    @Override
    public List<ProductImage> findByProductId(Long productId) {
        return storage.values().stream()
            .filter(pair -> pair.getSecond().getId().equals(productId))
            .filter(pair -> pair.getFirst().getDeleteAt() == null)
            .map(Pair::getFirst)
            .toList();
    }

    // todo: MVP 이후
    @Override
    public ProductImage findByThumbnailImage(Long productId) {
        return null;
    }
}
