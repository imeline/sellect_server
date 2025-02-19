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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "search_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // @Builder 사용 시 명확한 객체 생성
@SuperBuilder
public class SearchLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String searchKeyword; // 검색어

    private Long categoryId; // 선택한 카테고리 ID

    private Long brandId; // 선택한 브랜드 ID

    @Column(nullable = false, length = 255)
    private String sessionId; // 회원(userId) 또는 비회원(UUID 기반)

    @Column(nullable = false)
    private LocalDateTime timestamp; // 검색 실행 시간

    @Column(nullable = false)
    private int resultCount; // 검색 결과 개수

    @Column(nullable = false)
    private boolean filterApplied; // 필터링 여부 (true = 필터 적용됨)


    public static SearchLogEntity from(SearchLog searchLog) {
        return SearchLogEntity.builder()
            .id(searchLog.getId())
            .searchKeyword(searchLog.getSearchKeyword())
            .categoryId(searchLog.getCategoryId())
            .brandId(searchLog.getBrandId())
            .sessionId(searchLog.getSessionId())
            .timestamp(searchLog.getTimestamp())
            .resultCount(searchLog.getResultCount())
            .filterApplied(searchLog.isFilterApplied())
            .build();
    }

    public SearchLog toModel() {
        return SearchLog.builder()
            .id(this.id)
            .searchKeyword(this.searchKeyword)
            .categoryId(this.categoryId)
            .brandId(this.brandId)
            .timestamp(this.timestamp)
            .resultCount(this.resultCount)
            .filterApplied(this.filterApplied)
            .build();
    }
}
