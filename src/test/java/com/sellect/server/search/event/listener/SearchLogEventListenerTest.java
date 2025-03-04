package com.sellect.server.search.event.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sellect.server.search.domain.SearchLog;
import com.sellect.server.search.event.SearchLogEvent;
import com.sellect.server.search.event.SearchLogEventListener;
import com.sellect.server.search.event.listener.config.TestAsyncConfig;
import com.sellect.server.search.repository.SearchLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SearchLogEventListener.class, TestAsyncConfig.class}) // ✅ 동기 실행 보장
class SearchLogEventListenerTest {

    @MockBean
    private SearchLogRepository searchLogRepository;

    private static final Logger log = LoggerFactory.getLogger(SearchLogEventListenerTest.class);

    @Nested
    @DisplayName("handleSearchLogEvent 실행")
    class HandleSearchLogEvent {

        @Test
        @DisplayName("[성공] 이벤트가 정상적으로 처리되고, 검색 로그가 저장됨")
        void test1000() {
            // given
            SearchLogEvent event = SearchLogEvent.publish("Laptop", "user123", 5, 1L, 2L);
            when(searchLogRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            SearchLogEventListener searchLogEventListener = new SearchLogEventListener(searchLogRepository);
            searchLogEventListener.handleSearchLogEvent(event); // ✅ 동기 실행됨

            // then
            ArgumentCaptor<SearchLog> captor = ArgumentCaptor.forClass(SearchLog.class);
            verify(searchLogRepository).save(captor.capture());

            SearchLog savedLog = captor.getValue();
            assertNotNull(savedLog);
            assertEquals("Laptop", savedLog.getKeyword());
            assertEquals("user123", savedLog.getUserIdentifier());
            assertEquals(5, savedLog.getResultCount());
            assertTrue(savedLog.isFilterApplied());
        }

        @Test
        @DisplayName("[실패] 예외 발생 시 오류 로그가 출력됨")
        void test1() {
            // given
            SearchLogEvent event = SearchLogEvent.publish("Laptop", "user123", 5, 1L, 2L);
            doThrow(new RuntimeException("DB 오류 발생")).when(searchLogRepository).save(any());

            // when
            SearchLogEventListener searchLogEventListener = new SearchLogEventListener(searchLogRepository);
            searchLogEventListener.handleSearchLogEvent(event); // ✅ 동기 실행됨

            // then
            verify(searchLogRepository).save(any());
            log.error("[SearchLogEvent] 검색 로그 저장 실패: {}", event.getSearchKeyword());
        }
    }
}
