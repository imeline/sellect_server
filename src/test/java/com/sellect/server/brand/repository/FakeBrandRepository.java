package com.sellect.server.brand.repository;

import com.sellect.server.brand.domain.Brand;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FakeBrandRepository implements BrandRepository {

    private final List<Brand> data = new ArrayList<>();

    public Optional<Brand> findById(Long brandId) {
        return data.stream()
            .filter(brand -> brand.getId().equals(brandId))
            .filter(brand -> brand.getDeleteAt() == null)
            .findFirst();
    }

    @Override
    public List<Brand> findAll() {
        return new ArrayList<>(data);
    }

    @Override
    public List<Brand> findByNameContaining(String brandName) {
        return data.stream()
            .filter(brand -> brand.getName().contains(brandName))
            .filter(brand -> brand.getDeleteAt() == null)
            .toList();
    }

    public Brand save(Brand brand) {
        findById(brand.getId()).ifPresentOrElse(
            existingBrand -> {
                // 기존 데이터 업데이트 (삭제 후 재등록)
                data.remove(existingBrand);
                data.add(brand);
            },
            () -> data.add(brand) // 새로운 데이터 추가
        );

        return brand;
    }



    public void clear() {
        data.clear();
    }
}
