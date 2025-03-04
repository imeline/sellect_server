package com.sellect.server.search.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sellect.server.common.infrastructure.jwt.JwtFilter;
import com.sellect.server.common.resolver.AuthenticationResolver;
import com.sellect.server.config.JsonConfig;
import com.sellect.server.config.SecurityConfig;
import com.sellect.server.search.application.AutoCompleteService;
import com.sellect.server.search.application.SearchService;
import com.sellect.server.search.controller.response.SearchResponse;
import com.sellect.server.search.domain.SearchCondition;
import com.sellect.server.search.domain.SearchSortType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(controllers = SearchController.class)
@Import(JsonConfig.class) // JSON 직렬화 설정 적용
@ImportAutoConfiguration(
    exclude = {
        SecurityAutoConfiguration.class,
        ManagementWebSecurityAutoConfiguration.class,
        SecurityConfig.class,
        SecurityFilterChain.class
    }) // Spring Security 비활성화
class SearchControllerTest {

    // 기본 WebMvcTest 를 위한 설정들
    @MockBean
    JwtFilter jwtFilter;
    @MockBean
    SecurityFilterChain securityFilterChain;
    @MockBean
    AuthenticationResolver authenticationResolver;
    @MockBean
    SearchService searchService;
    @MockBean
    AutoCompleteService autoCompleteService;
    @MockBean
    org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMetamodelMappingContext;
    // 필수
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() throws ServletException, IOException {
        // 🔹 doFilter() 실행되도록 설정하여 FilterChain 정상 작동
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtFilter).doFilter(any(ServletRequest.class), any(ServletResponse.class),
            any(FilterChain.class));
    }

    @Nested
    @DisplayName("SearchController API 테스트")
    class SearchTotalTests {

        @Test
        @WithMockUser(username = "testUser", roles = {"USER"}) // 회원인 경우
        @DisplayName("test1000_ 상품 검색 API - 회원 요청 (200 OK)")
        void test1000() throws Exception {
            // Given
            String keyword = "테스트";
            int page = 0;
            int size = 20;

            SearchResponse searchResponse = new SearchResponse(
                "테스트 브랜드",
                "P12345",
                "https://example.com/image.jpg",
                "테스트 상품",
                new BigDecimal("2000.00")
            );

            List<SearchResponse> mockContent = List.of(searchResponse);
            PageImpl<SearchResponse> mockPage = new PageImpl<>(mockContent,
                PageRequest.of(page, size), 1L); // PageRequest 사용하여 size 설정

            when(searchService.searchTotal(anyString(), any(SearchCondition.class), anyInt(),
                anyInt(), any(SearchSortType.class)))
                .thenReturn(mockPage);

            // When & Then
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/search/products")
                    .param("keyword", keyword)
                    .param("page", String.valueOf(page))
                    .param("size", String.valueOf(size))
                    .param("sortType", "LATEST")
                    .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.is_success").value(true)) // snake_case 적용
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.result.content[0].brand_name").value("테스트 브랜드"))
                .andExpect(jsonPath("$.result.content[0].product_id").value("P12345"))
                .andExpect(jsonPath("$.result.content[0].image_url").value(
                    "https://example.com/image.jpg"))
                .andExpect(jsonPath("$.result.content[0].name").value("테스트 상품"))
                .andExpect(jsonPath("$.result.content[0].price").value(2000.00))
                .andExpect(jsonPath("$.result.page_number").value(0))
                .andExpect(jsonPath("$.result.page_size").value(20))   // 정상적으로 20으로 설정됨
                .andExpect(jsonPath("$.result.total_elements").value(1))
                .andExpect(jsonPath("$.result.total_pages").value(1))
                .andExpect(jsonPath("$.result.is_last").value(true));
        }

        @Test
        @WithAnonymousUser // 비회원 요청
        @DisplayName("test1001_ 상품 검색 API - 비회원 요청 (200 OK)")
        void test1001() throws Exception {
            // Given
            String keyword = "테스트";
            int page = 0;
            int size = 20;

            SearchResponse searchResponse = new SearchResponse(
                "테스트 브랜드",
                "P12345",
                "https://example.com/image.jpg",
                "테스트 상품",
                new BigDecimal("2000.00")
            );

            List<SearchResponse> mockContent = List.of(searchResponse);
            PageImpl<SearchResponse> mockPage = new PageImpl<>(mockContent, PageRequest.of(page, size), 1L); // PageRequest 사용하여 size 설정

            when(searchService.searchTotal(anyString(), any(SearchCondition.class), anyInt(), anyInt(), any(SearchSortType.class)))
                .thenReturn(mockPage);

            // When & Then
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/search/products")
                    .param("keyword", keyword)
                    .param("page", String.valueOf(page))
                    .param("size", String.valueOf(size))
                    .param("sortType", "LATEST")
                    .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.is_success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.result.content[0].brand_name").value("테스트 브랜드"))
                .andExpect(jsonPath("$.result.content[0].product_id").value("P12345"))
                .andExpect(jsonPath("$.result.content[0].image_url").value("https://example.com/image.jpg"))
                .andExpect(jsonPath("$.result.content[0].name").value("테스트 상품"))
                .andExpect(jsonPath("$.result.content[0].price").value(2000.00))
                .andExpect(jsonPath("$.result.page_number").value(0))
                .andExpect(jsonPath("$.result.page_size").value(20))
                .andExpect(jsonPath("$.result.total_elements").value(1))
                .andExpect(jsonPath("$.result.total_pages").value(1))
                .andExpect(jsonPath("$.result.is_last").value(true));
        }
    }

}
