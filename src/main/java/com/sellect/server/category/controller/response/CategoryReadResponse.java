package com.sellect.server.category.controller.response;

import com.sellect.server.category.domain.Category;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CategoryReadResponse{

    Long id;
    String name;
    @Builder.Default
    List<CategoryReadResponse> children = Collections.emptyList();

    public static CategoryReadResponse from(Category category, List<CategoryReadResponse> children) {
        return CategoryReadResponse.builder()
            .id(category.getId())
            .name(category.getName())
            .children(children)
            .build();
    }

    public static CategoryReadResponse from(Category category) {
        return from(category, Collections.emptyList()); // 기본적으로 자식이 없는 경우 빈 리스트
    }
}
