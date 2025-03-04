package com.sellect.server.product.repository;

import com.sellect.server.product.domain.Product;
import com.sellect.server.product.domain.ProductImage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FakeProductImageRepository implements ProductImageRepository {

    private final Map<Long, ImageWithProduct> storage = new HashMap<>();
    private long nextId = 1L; // ID 자동 증가를 위한 변수

    // ProductImage 와 Product 를 함께 저장하기 위한 내부 클래스
    private record ImageWithProduct(ProductImage image, Product product) {

    }

    @Override
    public ProductImage save(ProductImage productImage, Product product) {
        if (productImage.getId() == null) {
            // ID가 없으면 자동 생성
            productImage = ProductImage.builder()
                .id(nextId++)
                .product(product)
                .imageUrl(productImage.getImageUrl())
                .sequence(productImage.getSequence())
                .representative(productImage.isRepresentative())
                .build();
        }
        storage.put(productImage.getId(), new ImageWithProduct(productImage, product));
        return productImage;
    }

    @Override
    public Optional<ProductImage> findByProductImageId(Long productImageId) {
        return Optional.ofNullable(storage.get(productImageId))
            .filter(entry -> entry.image().getDeleteAt() == null)
            .map(ImageWithProduct::image);
    }

    @Override
    public List<ProductImage> findByProductId(Long productId) {
        return storage.values().stream()
            .filter(entry -> entry.product().getId().equals(productId))
            .map(ImageWithProduct::image)
            .filter(image -> image.getDeleteAt() == null)
            .toList();
    }

    @Override
    public ProductImage findByThumbnailImage(Long productId) {
        return storage.values().stream()
            .filter(entry -> entry.product().getId().equals(productId))
            .map(ImageWithProduct::image)
            .filter(ProductImage::isRepresentative)
            .filter(image -> image.getDeleteAt() == null)
            .findFirst()
            .orElse(null);
    }

    public void clear() {
        storage.clear();
        nextId = 1L; // 데이터 초기화 시 ID도 리셋
    }
}