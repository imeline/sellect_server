package com.sellect.server.search.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sellect.server.config.JpaConfig;
import com.sellect.server.config.JsonConfig;
import com.sellect.server.config.QueryDslConfig;
import com.sellect.server.search.controller.response.SearchResponse;
import com.sellect.server.search.domain.SearchCondition;
import com.sellect.server.search.domain.SearchSortType;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@Import({
    JpaConfig.class,
    JsonConfig.class,
    QueryDslConfig.class,
    SearchRepositoryImplTest.TestConfig.class
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // H2 설정 강제 적용
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.sql.init.mode=always",
    "spring.sql.init.schema-locations=classpath:schema.sql",
    "spring.sql.init.data-locations=classpath:data.sql"
})
public class SearchRepositoryImplTest {

    @Autowired
    private SearchRepositoryImpl sut;
    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestConfig {
        @Autowired
        private JPAQueryFactory queryFactory;

        @Bean
        public SearchRepositoryImpl searchRepositoryImpl() {
            return new SearchRepositoryImpl(queryFactory);
        }
    }

    @Nested
    @DisplayName("Search Total Test - 다양한 조건으로 상품 검색 테스트")
    class SearchTotalTests {

        @Test
        @DisplayName("상품명, 브랜드명, 카테고리명을 기준으로 검색하여 올바른 결과가 반환되어야 함")
        void test1000() {
            SearchCondition condition = SearchCondition.create(
                "Product1",
                1L, // data.sql에서 삽입된 category.id
                1L, // data.sql에서 삽입된 brand.id
                50_000,
                150_000
            );
            int page = 0;
            int size = 10;
            SearchSortType sortType = SearchSortType.PRICE_ASC;

            Page<SearchResponse> resultPage = sut.searchTotal(condition, page, size, sortType);

            assertThat(resultPage.getTotalElements()).isEqualTo(1);
            assertThat(resultPage.getContent()).hasSize(1);
            SearchResponse searchResponse = resultPage.getContent().get(0);
            assertThat(searchResponse.productId()).isEqualTo("1");
            assertThat(searchResponse.price()).isEqualTo(new BigDecimal("55000.00"));
            assertThat(searchResponse.brandName()).isEqualTo("Brand1");
            assertThat(searchResponse.imageUrl()).isEqualTo("http://example.com/image.jpg");
//            assertThat(searchResponse.productName()).isEqualTo("Product1");
        }
    }

    @Nested
    @DisplayName("정렬 기준에 따른 상품 검색 테스트")
    class SearchSortTests {

        @Test
        @DisplayName("가격 오름차순으로 정렬하여 상품 검색")
        void test1001() {
            SearchCondition condition = SearchCondition.create(
                "Product1",
                1L,
                1L,
                50_000,
                150_000
            );
            int page = 0;
            int size = 10;
            SearchSortType sortType = SearchSortType.PRICE_ASC;

            Page<SearchResponse> resultPage = sut.searchTotal(condition, page, size, sortType);

            assertThat(resultPage.getTotalElements()).isEqualTo(1);
            assertThat(resultPage.getContent()).hasSize(1);
            SearchResponse searchResponse = resultPage.getContent().get(0);
            assertThat(searchResponse.price()).isEqualTo(new BigDecimal("55000.00"));
        }

        @Test
        @DisplayName("가격 내림차순으로 정렬하여 상품 검색")
        void test1002() {
            SearchCondition condition = SearchCondition.create(
                "Product1",
                1L,
                1L,
                50_000,
                150_000
            );
            int page = 0;
            int size = 10;
            SearchSortType sortType = SearchSortType.PRICE_DESC;

            Page<SearchResponse> resultPage = sut.searchTotal(condition, page, size, sortType);

            assertThat(resultPage.getTotalElements()).isEqualTo(1);
            assertThat(resultPage.getContent()).hasSize(1);
            SearchResponse searchResponse = resultPage.getContent().get(0);
            assertThat(searchResponse.price()).isEqualTo(new BigDecimal("55000.00"));
        }
    }
}