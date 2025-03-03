package com.sellect.server.brand.repository;

import com.sellect.server.brand.domain.Brand;
import java.util.List;
import java.util.Optional;

public interface BrandRepository {

    Optional<Brand> findById(Long brandId);

    List<Brand> findAll();

    List<Brand> findByNameContaining(String brandName);
}
