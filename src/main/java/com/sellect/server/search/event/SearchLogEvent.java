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

    public static SearchLogEvent publish(String searchKeyword, String userIdentifier,
        int resultCount, Long categoryId, Long brandId) {

        boolean checkUsedFiltered = false;
        /*
        * 검색 완료 후에 실행되라도 정상적인 데이터가 들어오지 않을 수 있기에 이를 대비해서 NPE 추가
        * */
        // 유효성 검사 추가
        if (searchKeyword == null || searchKeyword.isEmpty()) {
            throw new IllegalArgumentException("검색 키워드는 null이거나 빈 문자열일 수 없습니다.");
        }
        if (userIdentifier == null || userIdentifier.isEmpty()) {
            throw new IllegalArgumentException("사용자 식별자는 null이거나 빈 문자열일 수 없습니다.");
        }
        if (resultCount < 0) {
            throw new IllegalArgumentException("검색 결과 개수는 음수가 될 수 없습니다.");
        }
        /*
         * 필터 사용 여부 체크 (카테고리 또는 브랜드 필터 적용 여부)
         * */
        if (categoryId != null || brandId != null) {
            checkUsedFiltered = true;
        }

        return SearchLogEvent.builder()
            .searchKeyword(searchKeyword)
            .userIdentifier(userIdentifier)
            .resultCount(resultCount)
            .filterApplied(checkUsedFiltered)
            .timestamp(LocalDateTime.now())
            .categoryId(categoryId)
            .brandId(brandId)
            .build();
    }
}
