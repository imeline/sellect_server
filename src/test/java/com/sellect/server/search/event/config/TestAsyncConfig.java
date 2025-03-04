package com.sellect.server.search.event.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SyncTaskExecutor;

@TestConfiguration
public class TestAsyncConfig {

    @Bean
    public SyncTaskExecutor asyncTaskExecutor() {
        return new SyncTaskExecutor(); // ✅ 동기 실행
    }
}
