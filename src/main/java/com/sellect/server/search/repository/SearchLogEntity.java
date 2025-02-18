package com.sellect.server.search.repository;

import com.sellect.server.search.domain.SearchLog;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "search_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // @Builder 사용 시 명확한 객체 생성
@Builder
public class SearchLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String keyword; // 검색어

    @Column(nullable = false, length = 255)
    private String userIdentifier; // 회원(userId) 또는 비회원(UUID 기반)

    @Column(nullable = false)
    private int resultCount; // 검색 결과 개수

    @Column(nullable = false)
    private boolean filterApplied; // 필터링 여부 (true = 필터 적용됨)

    @Column(nullable = false)
    private LocalDateTime timestamp; // 검색 실행 시간

    private Long categoryId; // 선택한 카테고리 ID

    private Long brandId; // 선택한 브랜드 ID

    public static SearchLogEntity from(SearchLog searchLog) {
        return SearchLogEntity.builder()
            .id(searchLog.getId())
            .keyword(searchLog.getKeyword())
            .userIdentifier(searchLog.getUserIdentifier())
            .resultCount(searchLog.getResultCount())
            .filterApplied(searchLog.isFilterApplied())
            .timestamp(searchLog.getTimestamp())
            .categoryId(searchLog.getCategoryId())
            .brandId(searchLog.getBrandId())
            .build();
    }

    public SearchLog toModel() {
        return SearchLog.builder()
            .id(this.id)
            .keyword(this.keyword)
            .userIdentifier(this.userIdentifier)
            .resultCount(this.resultCount)
            .filterApplied(this.filterApplied)
            .timestamp(this.timestamp)
            .categoryId(this.categoryId)
            .brandId(this.brandId)
            .build();
    }
}
