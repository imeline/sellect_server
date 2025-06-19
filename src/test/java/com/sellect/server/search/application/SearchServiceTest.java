package com.sellect.server.search.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.sellect.server.search.controller.response.SearchResponse;
import com.sellect.server.search.domain.SearchCondition;
import com.sellect.server.search.domain.SearchSortType;
import com.sellect.server.search.event.SearchLogEvent;
import com.sellect.server.search.repository.SearchRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

class SearchServiceTest {

    // SearchRepository 의 구현체가 복잡하기에 fakeRepository 보다 Mocking 하는걸로 대체했습니다.
    private final SearchRepository searchRepository = mock(SearchRepository.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final SearchService sut = new SearchService(searchRepository, eventPublisher);

    @Nested
    @DisplayName("searchTotal 실행")
    class SearchTotal {

        @Test
        @DisplayName("[성공] 검색 실행 후 이벤트 발행 확인")
        void test1000() {
            // given
            SearchCondition condition = SearchCondition.create("Laptop", 1L, 2L, null, null);
            Page<SearchResponse> mockPage = new PageImpl<>(List.of());

            // ✅ `eq(condition)`을 사용하여 정확한 인자가 전달되었는지 검증
            when(searchRepository.searchTotal(eq(condition), anyInt(), anyInt(), any())).thenReturn(mockPage);

            // when
            sut.searchTotal("user123", condition, 0, 10, SearchSortType.PRICE_ASC);

            // then
            verify(searchRepository).searchTotal(eq(condition), anyInt(), anyInt(), any());

            ArgumentCaptor<SearchLogEvent> eventCaptor = ArgumentCaptor.forClass(SearchLogEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            SearchLogEvent capturedEvent = eventCaptor.getValue();
            assertEquals("Laptop", capturedEvent.getSearchKeyword());
            assertEquals("user123", capturedEvent.getUserIdentifier());
            assertEquals(0, capturedEvent.getResultCount()); // 검색 결과 없음
            assertTrue(capturedEvent.isFilterApplied());

            // ✅ `categoryId`와 `brandId`도 검증
            assertEquals(1L, capturedEvent.getCategoryId());
            assertEquals(2L, capturedEvent.getBrandId());
        }
    }
}
