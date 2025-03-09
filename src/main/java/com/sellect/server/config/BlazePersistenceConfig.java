package com.sellect.server.config;

import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.JpqlFunctionGroup;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BlazePersistenceConfig {

    private static final Logger logger = LoggerFactory.getLogger(BlazePersistenceConfig.class);

    @Bean
    public CriteriaBuilderFactory criteriaBuilderFactory(EntityManagerFactory entityManagerFactory) {
        CriteriaBuilderConfiguration config = Criteria.getDefault();

        // MATCH 함수 정의
        JpqlFunction matchFunction = new JpqlFunction() {
            @Override
            public boolean hasArguments() {
                return true;
            }

            @Override
            public boolean hasParenthesesIfNoArguments() {
                return true;
            }

            @Override
            public Class<?> getReturnType(Class<?> firstArgumentType) {
                return Double.class; // MATCH ... AGAINST는 숫자형 점수를 반환
            }

            @Override
            public void render(FunctionRenderContext context) {
                context.addChunk("MATCH(");
                context.addArgument(0); // 대상 컬럼 (예: productEntity.name)
                context.addChunk(") AGAINST (");
                context.addArgument(1); // 검색어 (예: keyword)
                context.addChunk(" IN BOOLEAN MODE)");
            }
        };

        // MySQL과 MySQL8에 대해 함수 등록
        JpqlFunctionGroup matchFunctionGroup = new JpqlFunctionGroup("match_against");
        matchFunctionGroup.add("mysql", matchFunction);
        matchFunctionGroup.add("mysql8", matchFunction);
        config.registerFunction(matchFunctionGroup);

        logger.info("match_against function registered for MySQL and MySQL8");

        return config.createCriteriaBuilderFactory(entityManagerFactory);
    }
}