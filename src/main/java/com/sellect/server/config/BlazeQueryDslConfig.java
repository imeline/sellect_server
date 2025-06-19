package com.sellect.server.config;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.querydsl.BlazeJPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BlazeQueryDslConfig {

    private final CriteriaBuilderFactory criteriaBuilderFactory;

    @PersistenceContext
    private EntityManager entityManager;

    public BlazeQueryDslConfig(CriteriaBuilderFactory criteriaBuilderFactory) {
        this.criteriaBuilderFactory = criteriaBuilderFactory;
    }

    @Bean
    public BlazeJPAQueryFactory blazeJPAQueryFactory(EntityManager entityManager) {
        return new BlazeJPAQueryFactory(entityManager, criteriaBuilderFactory);
    }
}
