package com.sellect.server.search.application;

import com.sellect.server.search.controller.response.SearchResponse;
import com.sellect.server.search.domain.SearchCondition;
import com.sellect.server.search.domain.SearchSortType;
import com.sellect.server.search.event.SearchLogEvent;
import com.sellect.server.search.repository.SearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchRepository searchRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<SearchResponse> searchTotal(String userIdentifier,
        SearchCondition condition, int page, int size, SearchSortType sortType) {

        // 0. 상품 검색 실행
        Page<SearchResponse> searchProducts = searchRepository.searchTotal(condition, page, size,
            sortType);

        // 이벤트 발생 (로그 데이터)
        // 1. 유의미한 검색 키워드인지 판별을 위해
        int totalResults = (int) searchProducts.getTotalElements();

        // 2. 검색 로그 이벤트 발생
        eventPublisher.publishEvent(SearchLogEvent.publish(
            condition.getKeyword(),
            userIdentifier,
            totalResults,
            condition.getCategoryId(),
            condition.getBrandId()
        ));

        return searchProducts;
    }
}
