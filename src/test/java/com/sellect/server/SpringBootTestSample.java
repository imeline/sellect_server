package com.sellect.server;

import com.sellect.server.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.transaction.annotation.Transactional;

//@EnableJpaAuditing // Entity @LastModifiedDate 같은 JPA Auditing 기능 활성화  -- 이미 main 코드에 존재
@SpringBootTest
@ImportAutoConfiguration(
    exclude = {
        SecurityAutoConfiguration.class,
        ManagementWebSecurityAutoConfiguration.class,
        SecurityConfig.class}) // Spring Security 비활성화
@Transactional // 테스트 실행 후 DB 변경 사항 자동 롤백 (깨끗한 상태 유지)
class SpringBootTestSample {

    @MockBean
    private SecurityFilterChain securityFilterChain;


    @Test
    void contextLoads() {
    }

}
