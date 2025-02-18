package com.sellect.server.product.controller;

import com.sellect.server.auth.domain.User;
import com.sellect.server.common.infrastructure.annotation.AuthSeller;
import com.sellect.server.common.response.ApiResponse;
import com.sellect.server.product.application.ProductImageService;
import com.sellect.server.product.application.ProductService;
import com.sellect.server.product.controller.request.ProductImageModifyRequest;
import com.sellect.server.product.controller.request.ProductModifyRequest;
import com.sellect.server.product.controller.request.ProductRegisterRequest;
import com.sellect.server.product.controller.response.ProductModifyResponse;
import com.sellect.server.product.controller.response.ProductMultipleRegisterResponse;
import com.sellect.server.product.controller.response.ProductRegisterResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/api/v1")
@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductImageService productImageService;

    /**
     * 상품 단건 등록
     * */
    @PostMapping("/product")
    public ApiResponse<ProductRegisterResponse> register(
        @AuthSeller User seller,
        @RequestPart("requests") ProductRegisterRequest request,
        @RequestPart("images") List<MultipartFile> images) {

        ProductRegisterResponse response = productService.register(seller, request, images);
        return ApiResponse.ok(response);
    }

    /**
     * 상품 다건 등록
     */
    @PostMapping("/products")
    public ApiResponse<ProductMultipleRegisterResponse> register(
        @AuthSeller User seller,
        @RequestPart("requests") List<ProductRegisterRequest> requests,
        @RequestPart("images") List<MultipartFile> images) {

        ProductMultipleRegisterResponse result = productService.registerMultiple(seller, requests, images);
        return ApiResponse.ok(result);
    }

    /**
     * 상품 단건 수정 API
     */
    @PatchMapping("/products/{productId}")
    public ApiResponse<ProductModifyResponse> modify(
        @AuthSeller User seller,
        @PathVariable Long productId,
        @Valid @RequestBody ProductModifyRequest request
    ) {
        ProductModifyResponse response = productService.modify(seller.getId(), productId, request);
        return ApiResponse.ok(response);
    }

    /**
     * 상품 단건 삭제 API
     */
    @DeleteMapping("/products/{productId}")
    public ApiResponse<Void> remove(
        @AuthSeller User seller,
        @PathVariable Long productId
    ) {
        productService.remove(seller.getId(), productId);
        return ApiResponse.ok();
    }

    /**
     * 상품 이미지 수정 API
     * */
    @PostMapping("/products/images")
    public ApiResponse<Void> modifyProductImages(
        @AuthSeller User seller,
        @RequestPart("images") List<MultipartFile> images,
        @RequestPart("modify_request") ProductImageModifyRequest request
    ) {
        productImageService.modifyProductImages(seller.getId(), request, images);
        return ApiResponse.ok();
    }

}
