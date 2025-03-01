package com.sellect.server.search.repository;

import static com.sellect.server.brand.repository.QBrandEntity.brandEntity;
import static com.sellect.server.category.repository.QCategoryEntity.categoryEntity;
import static com.sellect.server.product.repository.QProductEntity.productEntity;
import static com.sellect.server.product.repository.QProductImageEntity.productImageEntity;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sellect.server.search.controller.response.SearchResponse;
import com.sellect.server.search.domain.SearchCondition;
import com.sellect.server.search.domain.SearchSortType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SearchRepositoryImpl implements SearchRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<SearchResponse> searchTotal(SearchCondition condition, int page, int size,
        SearchSortType sortType) {

        BooleanBuilder builder = new BooleanBuilder();

        // ✅ 검색 키워드 필터링 (카테고리명 → 브랜드명 → 상품명 순서)
        if (condition.getKeyword() != null && !condition.getKeyword().isEmpty()) {
            BooleanBuilder keywordBuilder = new BooleanBuilder();
            keywordBuilder.or(productEntity.categoryEntity.name.containsIgnoreCase(condition.getKeyword()));
            keywordBuilder.or(productEntity.brandEntity.name.containsIgnoreCase(condition.getKeyword()));
            keywordBuilder.or(productEntity.name.containsIgnoreCase(condition.getKeyword()));
            builder.and(keywordBuilder);
        }

        // ✅ 브랜드 필터링 (선택한 브랜드 ID)
        if (condition.getBrandId() != null) {
            builder.and(productEntity.brandEntity.id.eq(condition.getBrandId()));
        }

        // ✅ 카테고리 필터링 (대분류 → 중분류 → 소분류 포함)
        if (condition.getCategoryId() != null) {
            Integer categoryDepth = queryFactory
                .select(categoryEntity.depth)
                .from(categoryEntity)
                .where(categoryEntity.id.eq(condition.getCategoryId()))
                .fetchOne();

            List<Long> categoryIds = new ArrayList<>();

            if (categoryDepth != null) {
                if (categoryDepth == 1) { // ✅ 대분류 선택 시 → 중분류 & 소분류 포함
                    List<Long> middleCategories = queryFactory
                        .select(categoryEntity.id)
                        .from(categoryEntity)
                        .where(categoryEntity.parentId.eq(condition.getCategoryId()))
                        .fetch();

                    List<Long> subCategories = queryFactory
                        .select(categoryEntity.id)
                        .from(categoryEntity)
                        .where(categoryEntity.parentId.in(middleCategories))
                        .fetch();

                    categoryIds.addAll(middleCategories);
                    categoryIds.addAll(subCategories);
                } else if (categoryDepth == 2) { // ✅ 중분류 선택 시 → 소분류 포함
                    List<Long> subCategories = queryFactory
                        .select(categoryEntity.id)
                        .from(categoryEntity)
                        .where(categoryEntity.parentId.eq(condition.getCategoryId()))
                        .fetch();

                    categoryIds.addAll(subCategories);
                }
                categoryIds.add(condition.getCategoryId());
                builder.and(productEntity.categoryEntity.id.in(categoryIds));
            }
        }

        // ✅ 가격 필터링
        if (condition.getMinPrice() != null) {
            builder.and(productEntity.price.goe(condition.getMinPrice()));
        }
        if (condition.getMaxPrice() != null) {
            builder.and(productEntity.price.loe(condition.getMaxPrice()));
        }

        // 3. 대표 이미지 URL 서브 쿼리
        JPQLQuery<String> imageUrlSubQuery = JPAExpressions
            .select(productImageEntity.imageUrl)
            .from(productImageEntity)
            .where(productImageEntity.productEntity.id.eq(productEntity.id)
                .and(productImageEntity.representative.isTrue())
                .and(productImageEntity.deleteAt.isNull()))
            .orderBy(productImageEntity.createdAt.asc())
            .limit(1);

        // ✅ 검색 우선순위 정렬 (상품명 → 브랜드명 → 카테고리명)
        OrderSpecifier<Integer> searchPriority = new CaseBuilder()
            .when(productEntity.categoryEntity.name.containsIgnoreCase(condition.getKeyword())).then(1)
            .when(productEntity.brandEntity.name.containsIgnoreCase(condition.getKeyword())).then(2)
            .when(productEntity.name.containsIgnoreCase(condition.getKeyword())).then(3)
            .otherwise(4)
            .asc();

        // ✅ 정렬 기준 설정
        OrderSpecifier<?> orderSpecifier = getOrderSpecifier(sortType);

        // ✅ 최종 QueryDSL 검색 실행 (페이징 처리)
        List<SearchResponse> results = queryFactory
            .select(Projections.constructor(
                SearchResponse.class,
                productEntity.brandEntity.name,
                productEntity.id.stringValue(),
                imageUrlSubQuery, // ✅ 대표 이미지 URL 추가
                productEntity.name,
                productEntity.price
            ))
            .from(productEntity)
            .leftJoin(productEntity.brandEntity, brandEntity)
            .leftJoin(productEntity.categoryEntity, categoryEntity)
            .where(builder.and(productEntity.deleteAt.isNull()))
            .orderBy(searchPriority, orderSpecifier)
            .offset((long) page * size)
            .limit(size)
            .fetch();

        // ✅ 전체 검색 결과 개수
        Long total = Objects.requireNonNullElse(queryFactory
            .select(productEntity.count())
            .from(productEntity)
            .where(builder.and(productEntity.deleteAt.isNull()))
            .fetchOne(), 0L);

        return new PageImpl<>(results, PageRequest.of(page, size), total);
    }

    // ✅ 정렬 기준 설정 (가격 오름차순, 내림차순, 최신순)
    private OrderSpecifier<?> getOrderSpecifier(SearchSortType searchSortType) {
        return switch (searchSortType) {
            case PRICE_ASC -> productEntity.price.asc();
            case PRICE_DESC -> productEntity.price.desc();
            default -> productEntity.createdAt.desc();
        };
    }
}
