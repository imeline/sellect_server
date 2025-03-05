package com.sellect.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sellect.server.common.infrastructure.jwt.JwtFilter;
import com.sellect.server.common.resolver.AuthenticationResolver;
import com.sellect.server.config.JsonConfig;
import com.sellect.server.config.SecurityConfig;
import com.sellect.server.search.application.AutoCompleteService;
import com.sellect.server.search.application.SearchService;
import com.sellect.server.search.controller.SearchController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = SearchController.class)
@Import(JsonConfig.class) // JSON 직렬화 설정 적용
@ImportAutoConfiguration(
    exclude = {
        SecurityAutoConfiguration.class,
        ManagementWebSecurityAutoConfiguration.class,
        SecurityConfig.class,
        SecurityFilterChain.class

    }) // Spring Security 비활성화
class WebMvcControllerTestSample {

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

    @Autowired
    ObjectMapper objectMapper;

    // 필수
    @Autowired
    private MockMvc mockMvc;

//    @Test
//    void contextLoads() {
//    }
}