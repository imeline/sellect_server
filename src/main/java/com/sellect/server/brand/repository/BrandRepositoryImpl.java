package com.sellect.server.brand.repository;

import com.sellect.server.brand.domain.Brand;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BrandRepositoryImpl implements BrandRepository {

    private final BrandJpaRepository brandJpaRepository;

    @Override
    public Optional<Brand> findById(Long brandId) {
        return brandJpaRepository.findByIdAndDeleteAtIsNull(brandId)
            .map(BrandEntity::toModel);
    }

    @Override
    public List<Brand> findAll() {
        return brandJpaRepository.findAllByDeleteAtIsNull().stream().map(
            BrandEntity::toModel
        ).toList();
    }

}
