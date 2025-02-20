package com.sellect.server.category.application;

import com.sellect.server.category.controller.request.CategoryReadResponse;
import com.sellect.server.category.domain.Category;
import com.sellect.server.category.repository.CategoryRepository;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryReadResponse> readCategories() {
        List<Category> categories = categoryRepository.findAllOrderByDepth();
        return buildCategoryTree(categories);
    }

    private List<CategoryReadResponse> buildCategoryTree(List<Category> categories) {
        Map<Long, CategoryReadResponse> nodeMap = new HashMap<>();
        Map<Long, List<CategoryReadResponse>> childMap = new HashMap<>();
        List<CategoryReadResponse> rootNodes = new ArrayList<>();

        // 1. 모든 카테고리를 CategoryReadResponse 형태로 변환
        for (Category category : categories) {
            nodeMap.put(category.getId(), CategoryReadResponse.from(category, new ArrayList<>())); // ✅ 변경
            childMap.put(category.getId(), new ArrayList<>());
        }

        // 2. 부모-자식 관계 설정
        for (Category category : categories) {
            CategoryReadResponse node = nodeMap.get(category.getId());
            if (category.getParentId() == null) {
                rootNodes.add(node);
            } else {
                childMap.get(category.getParentId()).add(node);
            }
        }

        // 3. 부모 노드에 자식 추가 (객체 재사용)
        for (CategoryReadResponse parentNode : nodeMap.values()) {
            List<CategoryReadResponse> children = childMap.getOrDefault(parentNode.getId(), List.of());
            parentNode.getChildren().addAll(children); // 변경: 새로운 객체 생성 X, 기존 객체 수정
        }

        return rootNodes;
    }
}
