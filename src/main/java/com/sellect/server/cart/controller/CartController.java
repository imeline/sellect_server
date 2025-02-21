package com.sellect.server.cart.controller;

import com.sellect.server.auth.domain.User;
import com.sellect.server.cart.application.CartService;
import com.sellect.server.cart.controller.request.CartItemAddRequest;
import com.sellect.server.cart.controller.request.CartItemQuantityChangeRequest;
import com.sellect.server.cart.controller.response.CartItemQuantityChangeResponse;
import com.sellect.server.cart.controller.response.CardAddItemResponse;
import com.sellect.server.cart.controller.response.CartItemReadResponse;
import com.sellect.server.cart.controller.response.CartItemRetrieveResponse;
import com.sellect.server.cart.domain.CartItem;
import com.sellect.server.common.infrastructure.annotation.AuthUser;
import com.sellect.server.common.response.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CartController {

    private final CartService cartService;

    /*
    * 장바구니 상품 조회
    * */
    @GetMapping("/carts")
    public ApiResponse<List<CartItemReadResponse>> readAll(
        @AuthUser User user
    ) {
        List<CartItemReadResponse> result = cartService.readAll(user);
        return ApiResponse.ok(result);
    }


    /*
     * 장바구니 상품 추가 (기존 장바구니 없을 경우)
     * 장바구니 수량 +1 추가 (기존 장바구니에 있을 경우)
     * */
    @PutMapping("/cart")

    public ApiResponse<CardAddItemResponse> addCartItem(
        @AuthUser User user,
        @RequestBody CartItemAddRequest request) {

        CartItem result = cartService.addCartItem(user, request);
        return ApiResponse.ok(CardAddItemResponse.from(result));
    }

    @PatchMapping("/carts/{cartId}")
    public ApiResponse<CartItemQuantityChangeResponse> changeCartItemQuantity(
        @AuthUser User user,
        @PathVariable Long cartId,
        @RequestBody CartItemQuantityChangeRequest request) {

        CartItem result = cartService.changeCartItemQuantity(user.getId(), cartId, request);
        return ApiResponse.ok(CartItemQuantityChangeResponse.from(result));
    }

    @GetMapping("/cart")
    public ApiResponse<List<CartItemRetrieveResponse>> retrieveCartItems(@AuthUser User user) {

        List<CartItem> cartItems = cartService.retrieveCartItems(user.getId());
        return ApiResponse.ok(
            cartItems.stream()
                .map(CartItemRetrieveResponse::from)
                .toList()
        );
    }

    @DeleteMapping("/carts/{cartId}")
    public ApiResponse<Void> deleteCartItems(@AuthUser User user, @PathVariable Long cartId) {
        cartService.deleteCartItem(user.getId(), cartId);
        return ApiResponse.ok();
    }
}
