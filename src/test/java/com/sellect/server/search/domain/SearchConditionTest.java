package com.sellect.server.search.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SearchConditionTest {

    @Nested
    @DisplayName("SearchCondition 객체 생성")
    class Create {

        @Test
        @DisplayName("[성공] 모든 필드가 정상적으로 설정된 경우")
        void test1000() {
            // given
            SearchCondition condition = SearchCondition.create("Laptop", 1L, 2L, 100000, 200000);

            // then
            assertNotNull(condition);
            assertEquals("Laptop", condition.getKeyword());
            assertEquals(1L, condition.getCategoryId());
            assertEquals(2L, condition.getBrandId());
            assertEquals(100000, condition.getMinPrice());
            assertEquals(200000, condition.getMaxPrice());
        }

        @Test
        @DisplayName("[성공] 일부 필드가 null일 경우")
        void test1001() {
            // given
            SearchCondition condition = SearchCondition.create("Smartphone", null, 3L, 50000, null);

            // then
            assertNotNull(condition);
            assertEquals("Smartphone", condition.getKeyword());
            assertNull(condition.getCategoryId());
            assertEquals(3L, condition.getBrandId());
            assertEquals(50000, condition.getMinPrice());
            assertNull(condition.getMaxPrice());
        }

        @Test
        @DisplayName("[실패] 검색 키워드가 null인 경우 - 예외 발생")
        void test1() {
            // then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                SearchCondition.create(null, null, null, null, null)
            );

            assertEquals("검색 키워드는 null이거나 빈 문자열일 수 없습니다.", exception.getMessage());
        }

        @Test
        @DisplayName("[실패] 검색 키워드가 빈 문자열일 경우 - 예외 발생")
        void test2() {
            // then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                SearchCondition.create("", null, null, null, null)
            );

            assertEquals("검색 키워드는 null이거나 빈 문자열일 수 없습니다.", exception.getMessage());
        }

        @Test
        @DisplayName("[실패] 검색 키워드가 공백만 포함하는 경우 - 예외 발생")
        void test3() {
            // then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                SearchCondition.create("    ", null, null, null, null)
            );

            assertEquals("검색 키워드는 null이거나 빈 문자열일 수 없습니다.", exception.getMessage());
        }
    }
}
