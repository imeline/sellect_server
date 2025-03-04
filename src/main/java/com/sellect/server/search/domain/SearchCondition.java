package com.sellect.server.search.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE) // @Builder 사용 시 명확한 객체 생성
@Builder(access = AccessLevel.PRIVATE) // @Builder 사용 시 명확한 객체 생성
public class SearchCondition {

    // todo : 브랜드, 리뷰, 이미지 엔티티 생성 후 다시 돌아올 것
    private final String keyword;
    private final Long categoryId;
    private final Long brandId;
    private final Integer minPrice;
    private final Integer maxPrice;

    // static create() 메서드에서 검증 후 빌더 사용
    public static SearchCondition create(String keyword, Long categoryId, Long brandId, Integer minPrice, Integer maxPrice) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("검색 키워드는 null이거나 빈 문자열일 수 없습니다.");
        }

        return SearchCondition.builder()
            .keyword(keyword)
            .categoryId(categoryId)
            .brandId(brandId)
            .minPrice(minPrice)
            .maxPrice(maxPrice)
            .build();
    }
}
