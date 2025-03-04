package com.sellect.server.product.controller;

import com.sellect.server.auth.domain.User;
import com.sellect.server.common.infrastructure.annotation.AuthSeller;
import com.sellect.server.common.response.ApiResponse;
import com.sellect.server.product.application.ProductImageService;
import com.sellect.server.product.application.ProductService;
import com.sellect.server.product.controller.request.ProductImageModifyRequest;
import com.sellect.server.product.controller.request.ProductModifyRequest;
import com.sellect.server.product.controller.request.ProductRegisterRequest;
import com.sellect.server.product.controller.response.ProductDetailRetrieveResponse;
import com.sellect.server.product.controller.response.ProductDetailRetrieveBySellerResponse;
import com.sellect.server.product.controller.response.ProductModifyResponse;
import com.sellect.server.product.controller.response.ProductRegisterResponse;
import com.sellect.server.product.controller.response.SellerStatsRetrieveResponse;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
     * 상품 단건 등록 API (이미지 포함)
     */
//    @PostMapping("/product")
//    public ApiResponse<ProductRegisterResponse> register(
//        @AuthSeller User seller,
//        @RequestPart("register_request") ProductRegisterRequest request,
//        @RequestPart("images") List<MultipartFile> images) {
//
//        ProductRegisterResponse response = productService.register(seller, request, images);
//        return ApiResponse.ok(response);
//    }*

    /**
     * 상품 정보만 등록하는 API (이미지 별도)
     */
    @PostMapping("/product")
    public ApiResponse<ProductRegisterResponse> register(
        @AuthSeller User seller,
        @Valid @RequestPart("register_request") ProductRegisterRequest request) {

        ProductRegisterResponse response = productService.register(seller, request);
        return ApiResponse.ok(response);
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
     * 상품 이미지 수정 API (상품 이미지를 별도로 전송하는 경우 상품 이미지가 없을 수도 있음)
     */
    @PutMapping("/products/images")
    public ApiResponse<Void> modifyProductImages(
        @AuthSeller User seller,
        @RequestPart(value = "images", required = false) List<MultipartFile> images,
        @Valid @RequestPart(value = "modify_request") ProductImageModifyRequest request
    ) {
        productImageService.modifyProductImages(seller.getId(), request,
            images == null ? Collections.EMPTY_LIST : images);
        return ApiResponse.ok();
    }

    /**
     * 상품 상세 조회 API
     */
    @GetMapping("/products/{productId}")
    public ApiResponse<ProductDetailRetrieveResponse> retrieveDetail(
        @PathVariable Long productId
    ) {
        ProductDetailRetrieveResponse result = productService.retrieveDetail(productId);
        return ApiResponse.ok(result);
    }

    //========================= Seller 전용 =========================//
    @GetMapping("/seller/products")
    public ApiResponse<Page<ProductDetailRetrieveResponse>> retrieveAllBySeller(
        @AuthSeller User seller,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        Page<ProductDetailRetrieveResponse> result = productService.retrieveAllBySeller(seller, page,
            size);
        return ApiResponse.ok(result);
    }

    @GetMapping("/seller/products/{productId}")
    public ApiResponse<ProductDetailRetrieveBySellerResponse> retrieveDetailBySeller(
        @AuthSeller User seller,
        @PathVariable Long productId
    ) {
        ProductDetailRetrieveBySellerResponse result = productService.retrieveDetailBySeller(seller,
            productId);
        return ApiResponse.ok(result);
    }

    /*
    * 판매자 전용
    * */
    @GetMapping("/seller/stats")
    public ApiResponse<SellerStatsRetrieveResponse> retrieveStats(
        @AuthSeller User seller
    ) {
        SellerStatsRetrieveResponse result = productService.retrieveStats(seller);
        return ApiResponse.ok(result);
    }
}
