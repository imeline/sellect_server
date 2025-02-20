package com.sellect.server.product.controller.response;

import com.sellect.server.product.domain.Product;
import java.util.List;

public record ProductMultipleRegisterResponse(
    List<ProductRegisterSuccessResponse> successProducts,
    List<ProductRegisterFailureResponse> failedProducts
) {

    public static ProductMultipleRegisterResponse from(
        List<Product> successProducts,
        List<ProductRegisterFailureResponse> failedProducts
    ) {
        return new ProductMultipleRegisterResponse(
            ProductRegisterSuccessResponse.fromList(successProducts), // 성공 상품 리스트 반환
            failedProducts
        );
    }
}