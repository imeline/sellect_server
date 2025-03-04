package com.sellect.server.product.application;

import com.sellect.server.common.exception.CommonException;
import com.sellect.server.common.exception.enums.BError;
import com.sellect.server.product.controller.request.ImageContextCreateRequest;
import com.sellect.server.product.controller.request.ImageContextUpdateRequest;
import com.sellect.server.product.controller.request.ProductImageModifyRequest;
import com.sellect.server.product.domain.Product;
import com.sellect.server.product.domain.ProductImage;
import com.sellect.server.product.repository.ProductImageRepository;
import com.sellect.server.product.repository.ProductRepository;
import com.sellect.server.product.util.StorageUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProductImageService {

    private final StorageClient storageClient;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    @Transactional
    public void registerProductImage(Product product, ImageContextCreateRequest request, MultipartFile image) {
        String originalFilename = image.getOriginalFilename();
        if (originalFilename == null) {
            throw new CommonException(BError.NOT_EXIST, "file name");
        }

        String newFilename = StorageUtil.generateFileName(originalFilename);
        storageClient.store(image, newFilename);
        ProductImage productImage = ProductImage.register(product, storageClient.loadAsPath(newFilename), request);
        productImageRepository.save(productImage, product);
    }

    @Transactional
    public void modifyProductImages(
        Long sellerId,
        ProductImageModifyRequest request,
        List<MultipartFile> images) {

        Long productId = request.productId();
        List<Long> toDelete = request.productImageIdsToDelete();
        List<ImageContextUpdateRequest> toUpdate = request.productImagesToUpdate();

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "product"));

        if (!Objects.equals(product.getSeller().getId(), sellerId)) {
            throw new CommonException(BError.ACCESS_DENIED, "product");
        }

        // 상품 이미지 삭제
        toDelete.forEach(productImageId -> {
            ProductImage productImage = productImageRepository.findByProductImageId(productImageId)
                .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "product image"));
            productImageRepository.save(productImage.remove(), product);
        });

        // 새로운 상품 이미지를 이미지 저장소에 저장
        Map<String, String> newFileNames = new HashMap<>();
        images.forEach(image -> {
            String originalFilename = image.getOriginalFilename();
            if (originalFilename == null) {
                throw new CommonException(BError.NOT_EXIST, "file name");
            }

            String newFileName = StorageUtil.generateFileName(originalFilename);
            newFileNames.put(Objects.requireNonNull(image.getOriginalFilename())
                .substring(0, image.getOriginalFilename().lastIndexOf(".")), newFileName);
            storageClient.store(image, newFileName);
        });

        // 상품 이미지 수정 (이미지 순서 변경 및 새로운 이미지 DB에 추가)
        toUpdate.forEach(updateRequest -> {
            if (updateRequest.isNewImage()) {
                String imageUrl = storageClient.loadAsPath(newFileNames.get(updateRequest.uuid()));
                ProductImage productImage = ProductImage.registerWhenUpdate(product, imageUrl, updateRequest);
                productImageRepository.save(productImage, product);
            } else {
                ProductImage productImage = productImageRepository.findByProductImageId(updateRequest.productImageId())
                    .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "product image"));
                ProductImage updatedProductImage = productImage.update(updateRequest);
                productImageRepository.save(updatedProductImage, product);
            }
        });
    }
}
