package com.sellect.server.brand.repository;

import com.sellect.server.brand.domain.Brand;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FakeBrandRepository implements BrandRepository {

    private final List<Brand> data = new ArrayList<>();
    private long nextId = 1L; // ID 자동 증가를 위한 변수

    @Override
    public Optional<Brand> findById(Long brandId) {
        return data.stream()
            .filter(brand -> brand.getId() != null && brand.getId().equals(brandId))
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
        if (brand.getId() == null) {
            // 새로운 엔티티라면 ID 자동 할당 및 모든 필드 복사
            Brand newBrand = Brand.builder()
                .id(nextId++)
                .name(brand.getName())
                .createdAt(brand.getCreatedAt())
                .updatedAt(brand.getUpdatedAt())
                .deleteAt(brand.getDeleteAt())
                .build();
            data.add(newBrand);
            return newBrand;
        } else {
            // 기존 엔티티라면 업데이트
            findById(brand.getId()).ifPresentOrElse(
                existingBrand -> {
                    data.remove(existingBrand);
                    data.add(brand);
                },
                () -> data.add(brand) // ID가 있지만 데이터에 없으면 추가
            );
            return brand;
        }
    }

    public void clear() {
        data.clear();
        nextId = 1L; // 데이터 초기화 시 ID도 리셋
    }
}