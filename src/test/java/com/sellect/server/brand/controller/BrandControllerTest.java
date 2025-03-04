package com.sellect.server.brand.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sellect.server.brand.application.BrandService;
import com.sellect.server.brand.controller.response.BrandRetrieveResponse;
import com.sellect.server.brand.domain.Brand;
import com.sellect.server.brand.repository.FakeBrandRepository;
import com.sellect.server.common.infrastructure.jwt.JwtFilter;
import com.sellect.server.common.resolver.AuthenticationResolver;
import com.sellect.server.config.SecurityConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BrandController.class)
@ImportAutoConfiguration(
    exclude = {
        SecurityAutoConfiguration.class,
        ManagementWebSecurityAutoConfiguration.class,
        SecurityConfig.class,
        SecurityFilterAutoConfiguration.class,
    })
class BrandControllerTest {

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private SecurityFilterChain securityFilterChain;

    @MockBean
    private AuthenticationResolver authenticationResolver;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BrandService brandService;

    private final FakeBrandRepository brandRepository = new FakeBrandRepository();

    @Autowired
    private ObjectMapper objectMapper;

    private Brand brand;

    @BeforeEach
    void setUp() throws ServletException, IOException {
        brand = Brand.builder()
            .id(1L)
            .name("testBrand")
            .build();
        brandRepository.save(brand);

        // JwtFilter Mock 설정
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtFilter).doFilter(any(ServletRequest.class), any(ServletResponse.class),
            any(FilterChain.class));
    }

    @AfterEach
    void tearDown() {
        brandRepository.clear();
    }

    @Test
    @DisplayName("단일 브랜드 검색 시 정상적으로 반환된다")
    void retrieveBrandsByCategory_singleBrand_returnsSuccessfully() throws Exception {
        String brandName = "testBrand";
        List<BrandRetrieveResponse> mockResponse = List.of(
            BrandRetrieveResponse.from(brand)
        );

        when(brandService.retrieveBrandsContainingName("testBrand"))
            .thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/brands")
                .param("brand_name", brandName)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isSuccess").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.result").isArray())
            .andExpect(jsonPath("$.result[0].id").value(1))
            .andExpect(jsonPath("$.result[0].name").value("testBrand"));

    }

    @Test
    @DisplayName("여러 브랜드 검색 시 모든 결과가 정상적으로 반환된다")
    void retrieveBrandsByCategory_multipleBrands_returnsSuccessfully() throws Exception {
        String brandName = "brand";
        List<BrandRetrieveResponse> mockResponse = List.of(
            BrandRetrieveResponse.from(Brand.builder().id(1L).name("brand1").build()),
            BrandRetrieveResponse.from(Brand.builder().id(2L).name("brand2").build())
        );

        when(brandService.retrieveBrandsContainingName("brand"))
            .thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/brands")
                .param("brand_name", brandName)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isSuccess").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.result").isArray())
            .andExpect(jsonPath("$.result", hasSize(2)))
            .andExpect(jsonPath("$.result[0].id").value(1))
            .andExpect(jsonPath("$.result[0].name").value("brand1"))
            .andExpect(jsonPath("$.result[1].id").value(2))
            .andExpect(jsonPath("$.result[1].name").value("brand2"));
    }

    @Test
    @DisplayName("일치하는 브랜드가 없을 때 빈 리스트가 반환된다")
    void retrieveBrandsByCategory_noMatchingBrands_returnsEmptyList() throws Exception {
        String brandName = "nonexistent";
        List<BrandRetrieveResponse> mockResponse = Collections.emptyList();

        when(brandService.retrieveBrandsContainingName("nonexistent"))
            .thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/brands")
                .param("brand_name", brandName)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isSuccess").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.result").isArray())
            .andExpect(jsonPath("$.result").isEmpty());
    }

    @Test
    @DisplayName("brand_name 파라미터가 누락되면 400 Bad Request가 반환된다")
    void retrieveBrandsByCategory_missingBrandName_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/brands")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("대소문자 무시 검색 시 정상적으로 결과가 반환된다")
    void retrieveBrandsByCategory_caseInsensitiveSearch_returnsSuccessfully() throws Exception {
        String brandName = "TESTBRAND";
        List<BrandRetrieveResponse> mockResponse = List.of(
            BrandRetrieveResponse.from(brand)
        );

        when(brandService.retrieveBrandsContainingName("TESTBRAND"))
            .thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/brands")
                .param("brand_name", brandName)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isSuccess").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.result").isArray())
            .andExpect(jsonPath("$.result[0].id").value(1))
            .andExpect(jsonPath("$.result[0].name").value("testBrand"));
    }

}