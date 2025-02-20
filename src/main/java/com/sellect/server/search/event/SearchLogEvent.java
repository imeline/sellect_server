package com.sellect.server.search.event;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE) // @Builder 사용 시 명확한 객체 생성
@Builder
public class SearchLogEvent {
    private String searchKeyword;
    private String userIdentifier;
    private int resultCount;
    private boolean filterApplied;
    private LocalDateTime timestamp;
    private Long categoryId;
    private Long brandId;

    public static SearchLogEvent publish(String searchKeyword, String userIdentifier, int resultCount,
        boolean filterApplied,  Long categoryId, Long brandId) {
        return SearchLogEvent.builder()
            .searchKeyword(searchKeyword)
            .userIdentifier(userIdentifier)
            .resultCount(resultCount)
            .filterApplied(filterApplied)
            .timestamp(LocalDateTime.now())
            .categoryId(categoryId)
            .brandId(brandId)
            .build();
    }
}
