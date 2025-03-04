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
    private final StorageClient storageClient = new FakeStorageClient();
    private final ProductImageService sut = new ProductImageService(
        storageClient,
        productRepository,
        productImageRepository);

    private final Long sellerId = 1L;

    private Product product;
    private ProductImage productImage1;
    private ProductImage productImage2;
    private ProductImage productImage3;

    @BeforeEach
    void setUp() {
        product = Product.builder()
            .name("Test Product")
            .seller(User.builder()
                .id(sellerId)
                .build())
            .build();
        product = productRepository.save(product);

        // productImage1 -> productImage2 -> productImage3 (순서 보장)
        productImage1 = ProductImage.builder()
            .sequence(1)
            .imageUrl(FAKE_IMAGE_STORAGE_URL + "image1-uuid.jpg")
            .product(product)
            .build();
        productImage2 = ProductImage.builder()
            .sequence(2)
            .imageUrl(FAKE_IMAGE_STORAGE_URL + "image2-uuid.jpg")
            .product(product)
            .build();
        productImage3 = ProductImage.builder()
            .sequence(3)
            .imageUrl(FAKE_IMAGE_STORAGE_URL + "image3-uuid.jpg")
            .product(product)
            .build();

        productImage1 = productImageRepository.save(productImage1, product);
        productImage2 = productImageRepository.save(productImage2, product);
        productImage3 = productImageRepository.save(productImage3, product);
        storageClient.store(new FakeMultipartFile("image1-uuid.jpg"), "image1-uuid.jpg");
        storageClient.store(new FakeMultipartFile("image2-uuid.jpg"), "image2-uuid.jpg");
        storageClient.store(new FakeMultipartFile("image3-uuid.jpg"), "image3-uuid.jpg");
    }

    @AfterEach
    void tearDown() {
        productRepository.clear();
        productImageRepository.clear();
        storageClient.deleteAll();
    }

    @Nested
    @DisplayName("상품 이미지 수정 테스트")
    class ModifyTests {

        @Test
        @DisplayName("첫 번째 상품 이미지 삭제")
        void modifyProductImages_Success() {
            // Given
            ProductImageModifyRequest request = ProductImageModifyRequest.builder()
                .productId(product.getId())
                .productImageIdsToDelete(List.of(productImage1.getId()))
                .productImagesToUpdate(List.of(
                    ImageContextUpdateRequest.builder()
                        .productImageId(productImage2.getId())
                        .sequence(1)
                        .isNewImage(false)
                        .isRepresentative(true)
                        .build(),
                    ImageContextUpdateRequest.builder()
                        .productImageId(productImage3.getId())
                        .sequence(2)
                        .isNewImage(false)
                        .isRepresentative(false)
                        .build()))
                .build();

            // When
            sut.modifyProductImages(sellerId, request, Collections.emptyList());

            // Then
            assertThat(
                productImageRepository.findByProductImageId(productImage1.getId())).isEmpty();
            assertThat(productImageRepository.findByProductImageId(productImage2.getId()))
                .hasValueSatisfying(image -> {
                    assertThat(image.getSequence()).isEqualTo(1);
                    assertThat(image.isRepresentative()).isTrue();
                });
            assertThat(productImageRepository.findByProductImageId(productImage3.getId()))
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
                .productId(product.getId())
                .productImageIdsToDelete(List.of(productImage2.getId()))
                .productImagesToUpdate(List.of(
                    ImageContextUpdateRequest.builder()
                        .productImageId(productImage1.getId())
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
                        .productImageId(productImage3.getId())
                        .sequence(3)
                        .isNewImage(false)
                        .isRepresentative(false)
                        .build()))
                .build();

            // When
            sut.modifyProductImages(sellerId, request, List.of(newImage));

            // Then
            assertThat(
                productImageRepository.findByProductImageId(productImage2.getId())).isEmpty();
            assertThat(productImageRepository.findByProductImageId(productImage1.getId()))
                .hasValueSatisfying(image -> {
                    assertThat(image.getSequence()).isEqualTo(1);
                    assertThat(image.isRepresentative()).isTrue();
                });
            assertThat(productImageRepository.findByProductImageId(productImage3.getId()))
                .hasValueSatisfying(image -> {
                    assertThat(image.getSequence()).isEqualTo(3);
                    assertThat(image.isRepresentative()).isFalse();
                });
            productImageRepository.findByProductId(product.getId())
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
                .productId(product.getId())
                .productImageIdsToDelete(Collections.emptyList())
                .productImagesToUpdate(List.of(
                    ImageContextUpdateRequest.builder()
                        .productImageId(productImage1.getId())
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
                        .productImageId(productImage2.getId())
                        .sequence(3)
                        .isNewImage(false)
                        .isRepresentative(false)
                        .build(),
                    ImageContextUpdateRequest.builder()
                        .productImageId(productImage3.getId())
                        .sequence(4)
                        .isNewImage(false)
                        .isRepresentative(false)
                        .build()))
                .build();

            // When
            sut.modifyProductImages(sellerId, request, List.of(newImage));

            // Then
            assertThat(productImageRepository.findByProductImageId(productImage1.getId()))
                .hasValueSatisfying(image -> {
                    assertThat(image.getSequence()).isEqualTo(1);
                    assertThat(image.isRepresentative()).isTrue();
                });
            assertThat(productImageRepository.findByProductImageId(productImage2.getId()))
                .hasValueSatisfying(image -> {
                    assertThat(image.getSequence()).isEqualTo(3);
                    assertThat(image.isRepresentative()).isFalse();
                });
            assertThat(productImageRepository.findByProductImageId(productImage3.getId()))
                .hasValueSatisfying(image -> {
                    assertThat(image.getSequence()).isEqualTo(4);
                    assertThat(image.isRepresentative()).isFalse();
                });
            productImageRepository.findByProductId(product.getId()).forEach(image -> {
                if (image.getSequence() == 2) {
                    assertThat(image.getImageUrl()).contains(newImageUuid);
                }
            });
        }

        @Test
        @DisplayName("새로운 이미지를 추가하고 4번째 위치에 삽입. 단, 이미지 파일은 별도로 저장")
        void modifyProductImages_UpdateImageContextsAfterStoringImageFiles() {
            // Given
            String newImageFilename = "new-image-uuid.jpg";
            FakeMultipartFile multipartFile = new FakeMultipartFile(newImageFilename);
            storageClient.store(multipartFile, newImageFilename);
            ProductImageModifyRequest request = ProductImageModifyRequest.builder()
                .productId(product.getId())
                .productImageIdsToDelete(Collections.emptyList())
                .productImagesToUpdate(List.of(
                        ImageContextUpdateRequest.builder()
                            .sequence(4)
                            .isNewImage(true)
                            .filename(newImageFilename)
                            .build()
                    )
                )
                .build();

            // When
            sut.modifyProductImages(sellerId, request, Collections.emptyList());

            // Then
            List<ProductImage> updatedImages = productImageRepository.findByProductId(
                product.getId());
            assertThat(updatedImages).hasSize(4); // 기존 3개 + 새로운 이미지 1개
            assertThat(updatedImages)
                .filteredOn(image -> image.getSequence() == 4)
                .hasSize(1)
                .first()
                .satisfies(image -> {
                    assertThat(image.getImageUrl()).isEqualTo(
                        FAKE_IMAGE_STORAGE_URL + newImageFilename);
                    assertThat(image.isRepresentative()).isFalse();
                });
            // 기존 이미지들의 순서가 유지되는지 확인
            assertThat(updatedImages)
                .filteredOn(image -> image.getId().equals(productImage1.getId()))
                .hasSize(1)
                .first()
                .satisfies(image -> assertThat(image.getSequence()).isEqualTo(1));
            assertThat(updatedImages)
                .filteredOn(image -> image.getId().equals(productImage2.getId()))
                .hasSize(1)
                .first()
                .satisfies(image -> assertThat(image.getSequence()).isEqualTo(2));
            assertThat(updatedImages)
                .filteredOn(image -> image.getId().equals(productImage3.getId()))
                .hasSize(1)
                .first()
                .satisfies(image -> assertThat(image.getSequence()).isEqualTo(3));
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
                .id(product.getId())
                .name("Test Product")
                .seller(User.builder()
                    .id(anotherSellerId)
                    .build())
                .build();
            productRepository.save(anotherSellerProduct);

            ProductImageModifyRequest request = ProductImageModifyRequest.builder()
                .productId(product.getId())
                .build();

            // When & Then
            assertThatThrownBy(
                () -> sut.modifyProductImages(sellerId, request, Collections.emptyList()))
                .isInstanceOf(CommonException.class)
                .hasMessageContaining(BError.ACCESS_DENIED.getMessage("product"));
        }

        @Test
        @DisplayName("삭제 시 존재하지 않는 이미지로 접근하면 예외 발생")
        void modifyProductImages_ImageToDeleteNotFound() {
            // Given
            Long nonExistentId = 999L;
            ProductImageModifyRequest request = ProductImageModifyRequest.builder()
                .productId(product.getId())
                .productImageIdsToDelete(List.of(nonExistentId))
                .build();

            // When & Then
            assertThatThrownBy(
                () -> sut.modifyProductImages(sellerId, request, Collections.emptyList()))
                .isInstanceOf(CommonException.class)
                .hasMessageContaining(BError.NOT_EXIST.getMessage("product image"));
        }

        @Test
        @DisplayName("수정 시 존재하지 않는 이미지로 접근하면 예외 발생")
        void modifyProductImages_ImageToUpdateNotFound() {
            // Given
            Long nonExistentId = 999L;
            ProductImageModifyRequest request = ProductImageModifyRequest.builder()
                .productId(product.getId())
                .productImageIdsToDelete(Collections.emptyList())
                .productImagesToUpdate(List.of(
                    ImageContextUpdateRequest.builder()
                        .productImageId(nonExistentId)
                        .build()))
                .build();

            // When & Then
            assertThatThrownBy(
                () -> sut.modifyProductImages(sellerId, request, Collections.emptyList()))
                .isInstanceOf(CommonException.class)
                .hasMessageContaining(BError.NOT_EXIST.getMessage("product image"));
        }

        @Test
        @DisplayName("이미지 파일에 이름이 유효하지 않으면 예외 발생")
        void modifyProductImages_NoFileName() {
            // Given
            ProductImageModifyRequest request = ProductImageModifyRequest.builder()
                .productId(product.getId())
                .productImageIdsToDelete(Collections.emptyList())
                .productImagesToUpdate(List.of(
                    ImageContextUpdateRequest.builder()
                        .sequence(4)
                        .filename("new-image-uuid.jpg")
                        .isNewImage(true)
                        .isRepresentative(false)
                        .build()))
                .build();
            MultipartFile imageFile = new FakeMultipartFile("");

            // When & Then
            assertThatThrownBy(
                () -> sut.modifyProductImages(sellerId, request, List.of(imageFile)))
                .isInstanceOf(CommonException.class)
                .hasMessageContaining(BError.NOT_VALID.getMessage("file name"));
        }
    }
}
