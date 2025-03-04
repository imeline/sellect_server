package com.sellect.server.search.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SearchLogEventTest {

    @Test
    @DisplayName("[성공] SearchLogEvent 객체 생성 및 데이터 검증")
    void test1000() {
        // given
        SearchLogEvent event = SearchLogEvent.publish("Laptop", "user123", 5, true, 1L, 2L);

        // then
        assertNotNull(event);
        assertEquals("Laptop", event.getSearchKeyword());
        assertEquals("user123", event.getUserIdentifier());
        assertEquals(5, event.getResultCount());
        assertTrue(event.isFilterApplied());
    }

    @Nested
    @DisplayName("SearchLogEvent 생성 - publish")
    class Publish {

        @Test
        @DisplayName("[실패] 검색 키워드가 null일 경우 예외 발생")
        void test1() {
            // when & then
            assertThrows(IllegalArgumentException.class, () ->
                SearchLogEvent.publish(null, "user123", 5, true, 1L, 2L)
            );
        }

        @Test
        @DisplayName("[실패] 검색 키워드가 빈 문자열일 경우 예외 발생")
        void test2() {
            // when & then
            assertThrows(IllegalArgumentException.class, () ->
                SearchLogEvent.publish("", "user123", 5, true, 1L, 2L)
            );
        }

        @Test
        @DisplayName("[실패] 사용자 식별자가 null일 경우 예외 발생")
        void test3() {
            // when & then
            assertThrows(IllegalArgumentException.class, () ->
                SearchLogEvent.publish("Laptop", null, 5, true, 1L, 2L)
            );
        }

        @Test
        @DisplayName("[실패] 사용자 식별자가 빈 문자열일 경우 예외 발생")
        void test4() {
            // when & then
            assertThrows(IllegalArgumentException.class, () ->
                SearchLogEvent.publish("Laptop", "", 5, true, 1L, 2L)
            );
        }

        @Test
        @DisplayName("[실패] 검색 결과 수가 음수일 경우 예외 발생")
        void test5() {
            // when & then
            assertThrows(IllegalArgumentException.class, () ->
                SearchLogEvent.publish("Laptop", "user123", -1, true, 1L, 2L)
            );
        }
    }
}