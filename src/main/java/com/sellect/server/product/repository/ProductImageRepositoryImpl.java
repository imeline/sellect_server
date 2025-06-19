package com.sellect.server.product.repository;

import com.sellect.server.product.domain.Product;
import com.sellect.server.product.domain.ProductImage;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductImageRepositoryImpl implements ProductImageRepository {

    private final ProductImageJpaRepository productImageJpaRepository;

    @Override
    public ProductImage save(ProductImage productImage, Product product) {
        return productImageJpaRepository.save(ProductImageEntity.from(productImage, product)).toModel();
    }

    @Override
    public Optional<ProductImage> findByProductImageId(Long productId) {
        return productImageJpaRepository.findByIdAndDeleteAtIsNull(productId)
            .map(ProductImageEntity::toModel);
    }

    @Override
    public List<ProductImage> findByProductId(Long productId) {
        return productImageJpaRepository.findByProductEntityIdAndDeleteAtIsNull(productId).stream()
            .map(ProductImageEntity::toModel)
            .toList();
    }

    @Override
    public ProductImage findByThumbnailImage(Long productId) {
        return productImageJpaRepository.findFirstByProductEntityIdAndRepresentativeIsTrueAndDeleteAtIsNull(
            productId).toModel();
    }

}
