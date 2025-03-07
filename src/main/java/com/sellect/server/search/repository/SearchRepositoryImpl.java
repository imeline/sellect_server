package com.sellect.server.search.repository;

import static com.sellect.server.brand.repository.QBrandEntity.brandEntity;
import static com.sellect.server.category.repository.QCategoryEntity.categoryEntity;
import static com.sellect.server.product.repository.QProductEntity.productEntity;
import static com.sellect.server.product.repository.QProductImageEntity.productImageEntity;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.querydsl.BlazeJPAQuery;
import com.blazebit.persistence.querydsl.BlazeJPAQueryFactory;
import com.blazebit.persistence.querydsl.SetExpression;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPQLQuery;
import com.sellect.server.product.repository.QProductImageEntity;
import com.sellect.server.search.controller.response.SearchResponse;
import com.sellect.server.search.domain.SearchCondition;
import com.sellect.server.search.domain.SearchSortType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class SearchRepositoryImpl implements SearchRepository {

    private final BlazeJPAQueryFactory queryFactory;
    private final CriteriaBuilderFactory criteriaBuilderFactory;

    @PersistenceContext
    private EntityManager entityManager;

    private BooleanExpression contains(StringPath target, String searchWord) {
        if (searchWord == null || searchWord.isBlank()) {
            return null;
        }
        return Expressions.numberTemplate(Double.class, "function('match_against', {0}, {1})",
            target, searchWord).gt(0.0);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SearchResponse> searchTotal(SearchCondition condition, int page, int size, SearchSortType sortType) {
        BooleanBuilder filterBuilder = new BooleanBuilder();

        if (condition.getBrandId() != null) {
            filterBuilder.and(productEntity.brandEntity.id.eq(condition.getBrandId()));
        }
        if (condition.getCategoryId() != null) {
            List<Long> categoryIds = queryFactory
                .select(categoryEntity.id)
                .from(categoryEntity)
                .where(categoryEntity.id.eq(condition.getCategoryId())
                    .or(categoryEntity.parentId.eq(condition.getCategoryId())))
                .fetch();
            filterBuilder.and(productEntity.categoryEntity.id.in(categoryIds));
        }
        if (condition.getMinPrice() != null) {
            filterBuilder.and(productEntity.price.goe(condition.getMinPrice()));
        }
        if (condition.getMaxPrice() != null) {
            filterBuilder.and(productEntity.price.loe(condition.getMaxPrice()));
        }

        String keyword = condition.getKeyword();
        BooleanBuilder keywordBuilder = new BooleanBuilder();
        if (keyword != null && !keyword.isBlank()) {
            keywordBuilder.or(contains(productEntity.name, keyword));
            keywordBuilder.or(contains(brandEntity.name, keyword));
            keywordBuilder.or(contains(categoryEntity.name, keyword));
        } else {
            keywordBuilder.and(productEntity.id.isNotNull());
        }

        BooleanBuilder commonFilter = new BooleanBuilder();
        commonFilter.and(productEntity.deleteAt.isNull());

        // Union 쿼리 정의 (productEntity.id만 선택)
        BlazeJPAQuery<Long> unionQuery = new BlazeJPAQuery<>(entityManager, criteriaBuilderFactory);
        SetExpression<Long> matchedProducts = unionQuery.union(
            queryFactory.select(productEntity.id)
                .from(productEntity)
                .leftJoin(productEntity.brandEntity, brandEntity)
                .where(commonFilter.and(filterBuilder).and(contains(productEntity.name, keyword))),

            queryFactory.select(productEntity.id)
                .from(productEntity)
                .join(productEntity.brandEntity, brandEntity)
                .where(commonFilter.and(filterBuilder).and(contains(brandEntity.name, keyword))),

            queryFactory.select(productEntity.id)
                .from(productEntity)
                .join(productEntity.categoryEntity, categoryEntity)
                .leftJoin(productEntity.brandEntity, brandEntity)
                .where(commonFilter.and(filterBuilder).and(contains(categoryEntity.name, keyword)))
        );

        List<Long> productIds = matchedProducts.fetch().stream()
            .distinct()
            .collect(Collectors.toList());

        // 대표 이미지 서브쿼리 (고유 alias 사용)
        QProductImageEntity subProductImageEntity = new QProductImageEntity("subProductImageEntity");
        JPQLQuery<String> imageUrlSubQuery = queryFactory
            .select(subProductImageEntity.imageUrl)
            .from(subProductImageEntity)
            .where(subProductImageEntity.productEntity.id.eq(productEntity.id)
                .and(subProductImageEntity.representative.isTrue())
                .and(subProductImageEntity.deleteAt.isNull()))
            .orderBy(subProductImageEntity.createdAt.asc())
            .limit(1);

        // 최종 결과 쿼리
        List<SearchResponse> results = queryFactory
            .select(Projections.constructor(SearchResponse.class,
                brandEntity.name,
                productEntity.id.stringValue(),
                imageUrlSubQuery,
                productEntity.name,
                productEntity.price))
            .from(productEntity)
            .leftJoin(productEntity.brandEntity, brandEntity)
            .leftJoin(productImageEntity).on(productImageEntity.productEntity.id.eq(productEntity.id)
                .and(productImageEntity.representative.isTrue())
                .and(productImageEntity.deleteAt.isNull()))
            .where(productEntity.id.in(productIds))
            .orderBy(getOrderSpecifiers(sortType, condition))
            .offset((long) page * size)
            .limit(size)
            .fetch();

        // 전체 개수 계산
        Long total = queryFactory.select(productEntity.count())
            .from(productEntity)
            .leftJoin(productEntity.brandEntity, brandEntity)
            .leftJoin(productEntity.categoryEntity, categoryEntity)
            .where(commonFilter.and(filterBuilder).and(keywordBuilder))
            .fetchOne();

        return new PageImpl<>(results, PageRequest.of(page, size), Objects.requireNonNullElse(total, 0L));
    }


    private OrderSpecifier<?>[] getOrderSpecifiers(SearchSortType sortType, SearchCondition condition) {
        String keyword = condition.getKeyword();
        OrderSpecifier<Integer> priorityOrder = new OrderSpecifier<>(
            com.querydsl.core.types.Order.ASC,
            new CaseBuilder()
                .when(contains(productEntity.name, keyword)).then(1)
                .when(contains(brandEntity.name, keyword)).then(2)
                .when(contains(categoryEntity.name, keyword)).then(3)
                .otherwise(4)
        );
        return switch (sortType) {
            case PRICE_ASC -> new OrderSpecifier<?>[] { priorityOrder, productEntity.price.asc() };
            case PRICE_DESC -> new OrderSpecifier<?>[] { priorityOrder, productEntity.price.desc() };
            default -> new OrderSpecifier<?>[] { priorityOrder, productEntity.createdAt.desc() };
        };
    }
}