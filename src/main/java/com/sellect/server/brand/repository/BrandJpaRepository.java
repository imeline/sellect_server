package com.sellect.server.brand.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandJpaRepository extends JpaRepository<BrandEntity, Long> {

    Optional<BrandEntity> findByIdAndDeleteAtIsNull(Long brandId);

    List<BrandEntity> findAllByDeleteAtIsNull();

    List<BrandEntity> findByNameContainingAndDeleteAtIsNull(String brandName);
}