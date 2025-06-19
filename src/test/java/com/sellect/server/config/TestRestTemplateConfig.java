package com.sellect.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
@Import(JsonConfig.class)
public class TestRestTemplateConfig {

    private final ObjectMapper objectMapper;

    public TestRestTemplateConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    @Primary
    public TestRestTemplate testRestTemplate() {
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        testRestTemplate.getRestTemplate().getMessageConverters().add(0, new MappingJackson2HttpMessageConverter(objectMapper));
        return testRestTemplate;
    }
}
