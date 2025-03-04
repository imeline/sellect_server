package com.sellect.server.product.application;

import com.sellect.server.auth.domain.User;
import com.sellect.server.common.exception.CommonException;
import com.sellect.server.common.exception.enums.BError;
import static com.sellect.server.product.application.FakeStorageClient.FAKE_IMAGE_STORAGE_URL;
import com.sellect.server.product.controller.request.ImageContextUpdateRequest;
import com.sellect.server.product.controller.request.ProductImageModifyRequest;
import com.sellect.server.product.domain.Product;
import com.sellect.server.product.domain.ProductImage;
import com.sellect.server.product.repository.FakeProductImageRepository;
import com.sellect.server.product.repository.FakeProductRepository;
import java.util.Collections;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import org.springframework.web.multipart.MultipartFile;

class ProductImageServiceTest {

    private final FakeProductRepository productRepository = new FakeProductRepository();
    private final FakeProductImageRepository productImageRepository = new FakeProductImageRepository();
    private final FakeStorageClient storageClient = new FakeStorageClient();
    private final ProductImageService sut = new ProductImageService(
        storageClient,
        productRepository,
        productImageRepository);

    private final Long sellerId = 1L;
    private final Long productId = 100L;

    @BeforeEach
    void setUp() {
        Product product = Product.builder()
            .id(productId)
            .name("Test Product")
            .seller(User.builder()
                .id(sellerId)
                .build())
            .build();

        // productImage1 -> productImage2 -> productImage3 (순서 보장)
        ProductImage productImage1 = ProductImage.builder()
            .sequence(1)
            .imageUrl(FAKE_IMAGE_STORAGE_URL + "image1-uuid.jpg")
            .product(product)
            .build();
        ProductImage productImage2 = ProductImage.builder()
            .sequence(2)
            .imageUrl(FAKE_IMAGE_STORAGE_URL + "image2-uuid.jpg")
            .product(product)
            .build();
        ProductImage productImage3 = ProductImage.builder()
            .sequence(3)
            .imageUrl(FAKE_IMAGE_STORAGE_URL + "image3-uuid.jpg")
            .product(product)
            .build();

        productRepository.save(product);
        productImageRepository.save(productImage1, product);
        productImageRepository.save(productImage2, product);
        productImageRepository.save(productImage3, product);
        storageClient.store(mock(MultipartFile.class), "image1-uuid.jpg");
        storageClient.store(mock(MultipartFile.class), "image2-uuid.jpg");
        storageClient.store(mock(MultipartFile.class), "image3-uuid.jpg");
    }

    @AfterEach
    void tearDown() {
        productRepository.clear();
        productImageRepository.clear();
    }

    @Nested
    @DisplayName("상품 이미지 수정 테스트")
    class ModifyTests {

        @Test
        @DisplayName("첫 번째 상품 이미지 삭제")
        void modifyProductImages_Success() {
            // Given
            ProductImageModifyRequest request = ProductImageModifyRequest.builder()
                .productId(productId)
                .productImageIdsToDelete(List.of(1L))
                .productImagesToUpdate(List.of(
                    ImageContextUpdateRequest.builder()
                        .productImageId(2L)
                        .sequence(1)
                        .isNewImage(false)
                        .isRepresentative(true)
                        .build(),
                    ImageContextUpdateRequest.builder()
                        .productImageId(3L)
                        .sequence(2)
                        .isNewImage(false)
                        .isRepresentative(false)
                        .build()))
                .build();

            // When
            sut.modifyProductImages(sellerId, request, Collections.emptyList());

            // Then
            assertThat(productImageRepository.findByProductImageId(1L)).isEmpty();
            assertThat(productImageRepository.findByProductImageId(2L))
                .hasValueSatisfying(image -> {
                    assertThat(image.getSequence()).isEqualTo(1);
                    assertThat(image.isRepresentative()).isTrue();
                });
            assertThat(productImageRepository.findByProductImageId(3L))
                .hasValueSatisfying(image -> {
                    assertThat(image.getSequence()).isEqualTo(2);
                    assertThat(image.isRepresentative()).isFalse();
                });
        }

        @Test
        @DisplayName("두 번째 상품 이미지 삭제 후 두 번째 위치에 새로운 상품 이미지 삽입")
        void modifyProductImages_DeleteSecondImageAndInsertNewImage() {
            // Given
            String newImageUuid = "new-image-uuid";
            MultipartFile newImage = mock(MultipartFile.class);
            given(newImage.getOriginalFilename()).willReturn("new-image-uuid.jpg");

            ProductImageModifyRequest request = ProductImageModifyRequest.builder()
                .productId(productId)
                .productImageIdsToDelete(List.of(2L))
                .productImagesToUpdate(List.of(
                    ImageContextUpdateRequest.builder()
                        .productImageId(1L)
                        .sequence(1)
                        .isNewImage(false)
                        .isRepresentative(true)
                        .build(),
                    ImageContextUpdateRequest.builder()
                        .uuid(newImageUuid)
                        .sequence(2)
                        .isNewImage(true)
                        .isRepresentative(false)
                        .build(),
                    ImageContextUpdateRequest.builder()
                        .productImageId(3L)
                        .sequence(3)
                        .isNewImage(false)
                        .isRepresentative(false)
                        .build()))
                .build();

            // When
            sut.modifyProductImages(sellerId, request, List.of(newImage));

            // Then
            assertThat(productImageRepository.findByProductImageId(2L)).isEmpty();
            assertThat(productImageRepository.findByProductImageId(1L))
                .hasValueSatisfying(image -> {
                    assertThat(image.getSequence()).isEqualTo(1);
                    assertThat(image.isRepresentative()).isTrue();
                });
            assertThat(productImageRepository.findByProductImageId(3L))
                .hasValueSatisfying(image -> {
                    assertThat(image.getSequence()).isEqualTo(3);
                    assertThat(image.isRepresentative()).isFalse();
                });
            productImageRepository.findByProductId(productId)
                .forEach(image -> {
                    if (image.getSequence() == 2) {
                        assertThat(image.getImageUrl()).contains(newImageUuid);
                    }
                });
        }

        @Test
        @DisplayName("첫 번째와 두 번째 이미지 사이에 새로운 이미지 삽입")
        void modifyProductImages_InsertNewImageBetweenFirstAndSecondImage() {
            // Given
            String newImageUuid = "new-image-uuid";
            MultipartFile newImage = mock(MultipartFile.class);
            given(newImage.getOriginalFilename()).willReturn("new-image-uuid.jpg");

            ProductImageModifyRequest request = ProductImageModifyRequest.builder()
                .productId(productId)
                .productImageIdsToDelete(Collections.emptyList())
                .productImagesToUpdate(List.of(
                    ImageContextUpdateRequest.builder()
                        .productImageId(1L)
                        .sequence(1)
                        .isNewImage(false)
                        .isRepresentative(true)
                        .build(),
                    ImageContextUpdateRequest.builder()
                        .uuid(newImageUuid)
                        .sequence(2)
                        .isNewImage(true)
                        .isRepresentative(false)
                        .build(),
                    ImageContextUpdateRequest.builder()
                        .productImageId(2L)
                        .sequence(3)
                        .isNewImage(false)
                        .isRepresentative(false)
                        .build(),
                    ImageContextUpdateRequest.builder()
                        .productImageId(3L)
                        .sequence(4)
                        .isNewImage(false)
                        .isRepresentative(false)
                        .build()))
                .build();

            // When
            sut.modifyProductImages(sellerId, request, List.of(newImage));

            // Then
            assertThat(productImageRepository.findByProductImageId(1L))
                .hasValueSatisfying(image -> {
                    assertThat(image.getSequence()).isEqualTo(1); // 1 -> 2?
                    assertThat(image.isRepresentative()).isTrue();
                });
            assertThat(productImageRepository.findByProductImageId(2L))
                .hasValueSatisfying(image -> {
                    assertThat(image.getSequence()).isEqualTo(3);
                    assertThat(image.isRepresentative()).isFalse();
                });
            assertThat(productImageRepository.findByProductImageId(3L))
                .hasValueSatisfying(image -> {
                    assertThat(image.getSequence()).isEqualTo(4);
                    assertThat(image.isRepresentative()).isFalse();
                });
            productImageRepository.findByProductId(productId).forEach(image -> {
                if (image.getSequence() == 2) {
                    assertThat(image.getImageUrl()).contains(newImageUuid);
                }
            });
        }

        @Test
        @DisplayName("상품이 존재하지 않으면 예외 발생")
        void modifyProductImages_ProductNotFound() {
            // Given
            Long nonExistentProductId = 999L;
            ProductImageModifyRequest request = ProductImageModifyRequest.builder()
                .productId(nonExistentProductId)
                .build();

            // When & Then
            assertThatThrownBy(
                () -> sut.modifyProductImages(sellerId, request, Collections.emptyList()))
                .isInstanceOf(CommonException.class)
                .hasMessageContaining(BError.NOT_EXIST.getMessage("product"));
        }

        @Test
        @DisplayName("판매자 권한이 없으면 예외 발생")
        void modifyProductImages_Forbidden() {
            // Given
            Long anotherSellerId = 999L;
            Product anotherSellerProduct = Product.builder()
                .id(productId)
                .name("Test Product")
                .seller(User.builder()
                    .id(anotherSellerId)
                    .build())
                .build();
            productRepository.save(anotherSellerProduct);

            ProductImageModifyRequest request = ProductImageModifyRequest.builder()
                .productId(productId)
                .build();

            // When & Then
            assertThatThrownBy(
                () -> sut.modifyProductImages(sellerId, request, Collections.emptyList()))
                .isInstanceOf(CommonException.class)
                .hasMessageContaining(BError.ACCESS_DENIED.getMessage("product"));
        }

        @Test
        @DisplayName("삭제할 이미지가 존재하지 않으면 예외 발생")
        void modifyProductImages_ImageToDeleteNotFound() {
            // Given
            Long nonExistentUuid = 999L;
            ProductImageModifyRequest request = ProductImageModifyRequest.builder()
                .productId(productId)
                .productImageIdsToDelete(List.of(nonExistentUuid))
                .build();

            // When & Then
            assertThatThrownBy(
                () -> sut.modifyProductImages(sellerId, request, Collections.emptyList()))
                .isInstanceOf(CommonException.class)
                .hasMessageContaining(BError.NOT_EXIST.getMessage("product image"));
        }
    }
}
