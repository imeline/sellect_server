package com.sellect.server.search.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SearchLogEventTest {

    @Nested
    @DisplayName("SearchLogEvent 생성 - publish")
    class Publish {

        @Test
        @DisplayName("[성공] SearchLogEvent 객체 생성 및 데이터 검증")
        void test1000() {
            // given
            SearchLogEvent event = SearchLogEvent.publish("Laptop", "user123", 5, 1L, 2L);

            // then
            assertNotNull(event);
            assertEquals("Laptop", event.getSearchKeyword());
            assertEquals("user123", event.getUserIdentifier());
            assertEquals(5, event.getResultCount());
            assertTrue(event.isFilterApplied()); // categoryId와 brandId가 존재하므로 true
        }

        @Test
        @DisplayName("[성공] 필터가 적용되지 않은 경우 filterApplied=false 확인")
        void test1001() {
            // given
            SearchLogEvent event = SearchLogEvent.publish("Phone", "user456", 10, null, null);

            // then
            assertNotNull(event);
            assertEquals("Phone", event.getSearchKeyword());
            assertEquals("user456", event.getUserIdentifier());
            assertEquals(10, event.getResultCount());
            assertFalse(event.isFilterApplied()); // categoryId, brandId가 null이므로 false
        }

        @Test
        @DisplayName("[성공] 카테고리 ID만 존재할 경우 filterApplied=true")
        void test1002() {
            // given
            SearchLogEvent event = SearchLogEvent.publish("Phone", "user456", 10, 1L, null);

            // then
            assertNotNull(event);
            assertEquals("Phone", event.getSearchKeyword());
            assertEquals("user456", event.getUserIdentifier());
            assertEquals(10, event.getResultCount());
            assertTrue(event.isFilterApplied()); // categoryId, brandId가 null이므로 false
        }

        @Test
        @DisplayName("[성공] 브랜드 ID만 존재할 경우 filterApplied=true")
        void test1003() {
            // given
            SearchLogEvent event = SearchLogEvent.publish("Phone", "user456", 10, null, 2L);

            // then
            assertNotNull(event);
            assertEquals("Phone", event.getSearchKeyword());
            assertEquals("user456", event.getUserIdentifier());
            assertEquals(10, event.getResultCount());
            assertTrue(event.isFilterApplied()); // categoryId, brandId가 null이므로 false
        }

        @Test
        @DisplayName("[실패] 검색 키워드가 null일 경우 예외 발생")
        void test1() {
            // when & then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                SearchLogEvent.publish(null, "user123", 5, 1L, 2L)
            );

            assertEquals("검색 키워드는 null이거나 빈 문자열일 수 없습니다.", exception.getMessage());
        }

        @Test
        @DisplayName("[실패] 검색 키워드가 빈 문자열일 경우 예외 발생")
        void test2() {
            // when & then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                SearchLogEvent.publish("", "user123", 5, 1L, 2L)
            );

            assertEquals("검색 키워드는 null이거나 빈 문자열일 수 없습니다.", exception.getMessage());
        }

        @Test
        @DisplayName("[실패] 사용자 식별자가 null일 경우 예외 발생")
        void test3() {
            // when & then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                SearchLogEvent.publish("Laptop", null, 5, 1L, 2L)
            );

            assertEquals("사용자 식별자는 null이거나 빈 문자열일 수 없습니다.", exception.getMessage());
        }

        @Test
        @DisplayName("[실패] 사용자 식별자가 빈 문자열일 경우 예외 발생")
        void test4() {
            // when & then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                SearchLogEvent.publish("Laptop", "", 5, 1L, 2L)
            );

            assertEquals("사용자 식별자는 null이거나 빈 문자열일 수 없습니다.", exception.getMessage());
        }

        @Test
        @DisplayName("[실패] 검색 결과 수가 음수일 경우 예외 발생")
        void test5() {
            // when & then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                SearchLogEvent.publish("Laptop", "user123", -1, 1L, 2L)
            );

            assertEquals("검색 결과 개수는 음수가 될 수 없습니다.", exception.getMessage());
        }
    }
}
