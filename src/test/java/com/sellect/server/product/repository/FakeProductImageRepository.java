package com.sellect.server.product.repository;

import com.sellect.server.product.domain.Product;
import com.sellect.server.product.domain.ProductImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    @Override
    public ProductImage findByThumbnailImage(Long productId) {
        return storage.values().stream()
            .filter(pair -> pair.getSecond().getId().equals(productId)) // 해당 productId인지 확인
            .map(Pair::getFirst) // ProductImage 추출
            .filter(ProductImage::isRepresentative) // 대표 이미지인지 확인
            .filter(image -> image.getDeleteAt() == null) // 삭제되지 않은 이미지인지 확인
            .findFirst() // 첫 번째 매칭된 데이터 선택
            .orElse(null); // 없으면 null 반환
    }
}
