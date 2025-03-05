package com.sellect.server.category.repository;

import com.sellect.server.category.domain.Category;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FakeCategoryRepository implements CategoryRepository {

    private final List<Category> data = new ArrayList<>();
    private long nextId = 1L; // ID 자동 증가를 위한 변수

    public Category save(Category category) {
        if (category.getId() == null) {
            // 새로운 엔티티라면 ID 자동 할당 및 모든 필드 복사
            Category newCategory = Category.builder()
                .id(nextId++)
                .name(category.getName())
                .parentId(category.getParentId())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .deleteAt(category.getDeleteAt())
                .build();
            data.add(newCategory);
            return newCategory;
        } else {
            // 기존 엔티티라면 업데이트
            findById(category.getId()).ifPresentOrElse(
                existingCategory -> {
                    data.remove(existingCategory);
                    data.add(category);
                },
                () -> data.add(category) // ID가 있지만 데이터에 없으면 추가
            );
            return category;
        }
    }

    @Override
    public Optional<Category> findById(Long categoryId) {
        return data.stream()
            .filter(category -> category.getId() != null && category.getId().equals(categoryId))
            .filter(category -> category.getDeleteAt() == null)
            .findFirst();
    }

    @Override
    public List<Category> findAllOrderByDepth() {
        return new ArrayList<>(data);
    }

    public void clear() {
        data.clear();
        nextId = 1L; // 데이터 초기화 시 ID도 리셋
    }
}