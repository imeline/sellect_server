package com.sellect.server.product.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface ProductImageJpaRepository extends JpaRepository<ProductImageEntity, Long> {

    Optional<ProductImageEntity> findByIdAndDeleteAtIsNull(Long id);

    List<ProductImageEntity> findByProductEntityIdAndDeleteAtIsNull(
        @Param("productId") Long productId);

    ProductImageEntity findFirstByProductEntityIdAndRepresentativeIsTrueAndDeleteAtIsNull(
        Long productId);
}
