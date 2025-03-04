package com.sellect.server.brand.application;

import com.sellect.server.brand.controller.response.BrandRetrieveResponse;
import com.sellect.server.brand.domain.Brand;
import com.sellect.server.brand.repository.FakeBrandRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BrandServiceTest {

    private final FakeBrandRepository fakeBrandRepository = new FakeBrandRepository();
    private final BrandService sut = new BrandService(fakeBrandRepository);

    @BeforeEach
    void setUp() {
        fakeBrandRepository.clear(); // 각 테스트 전에 데이터 초기화
        Brand brand1 = Brand.builder()
            .id(1L)
            .name("apple")
            .deleteAt(null)
            .build();
        Brand brand2 = Brand.builder()
            .id(2L)
            .name("banana")
            .deleteAt(LocalDateTime.now()) // 해당 카테고리 삭제
            .build();
        Brand brand3 = Brand.builder()
            .id(3L)
            .name("cherry")
            .deleteAt(null)
            .build();
        Brand brand4 = Brand.builder()
            .id(4L)
            .name("apricot")
            .deleteAt(null)
            .build();

        fakeBrandRepository.save(brand1);
        fakeBrandRepository.save(brand2);
        fakeBrandRepository.save(brand3);
        fakeBrandRepository.save(brand4);
    }

    @Nested
    @DisplayName("브랜드 조회 테스트")
    class RetrieveBrandsContainingName {

        @Test
        @DisplayName("이름에 특정 문자열을 포함하는 삭제되지 않은 브랜드만 반환")
        void returnsOnlyNonDeletedBrandsContainingName() {
            // Given
            String brandName = "ap";

            // When
            List<BrandRetrieveResponse> result = sut.retrieveBrandsContainingName(brandName);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result)
                .extracting(BrandRetrieveResponse::name)
                .containsExactlyInAnyOrder("apple", "apricot");
            assertThat(result)
                .extracting(BrandRetrieveResponse::name)
                .doesNotContain("banana", "cherry");
        }

        @Test
        @DisplayName("일치하는 브랜드가 없으면 빈 리스트 반환")
        void returnsEmptyListWhenNoMatch() {
            // Given
            String brandName = "xyz";

            // When
            List<BrandRetrieveResponse> result = sut.retrieveBrandsContainingName(brandName);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("데이터가 없으면 빈 리스트 반환")
        void returnsEmptyListWhenNoData() {
            // Given
            fakeBrandRepository.clear(); // 데이터 모두 삭제
            String brandName = "ap";

            // When
            List<BrandRetrieveResponse> result = sut.retrieveBrandsContainingName(brandName);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("삭제된 브랜드는 제외됨")
        void excludesDeletedBrands() {
            // Given
            String brandName = "banana";

            // When
            List<BrandRetrieveResponse> result = sut.retrieveBrandsContainingName(brandName);

            // Then
            assertThat(result).isEmpty(); // "banana"는 삭제됨
        }

        @Test
        @DisplayName("이름이 null이 아닌 경우에도 정상 동작")
        void worksWithNonNullName() {
            // Given
            String brandName = "cherry";

            // When
            List<BrandRetrieveResponse> result = sut.retrieveBrandsContainingName(brandName);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("cherry");
        }
    }
}