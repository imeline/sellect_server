package com.sellect.server.cart.application;

import com.sellect.server.auth.domain.User;
import com.sellect.server.cart.controller.request.CartItemAddRequest;
import com.sellect.server.cart.controller.request.CartItemQuantityChangeRequest;
import com.sellect.server.cart.controller.response.CartItemReadResponse;
import com.sellect.server.cart.domain.CartItem;
import com.sellect.server.cart.repository.CartItemRepository;
import com.sellect.server.common.exception.CommonException;
import com.sellect.server.common.exception.enums.BError;
import com.sellect.server.product.domain.Product;
import com.sellect.server.product.repository.ProductImageRepository;
import com.sellect.server.product.repository.ProductRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    @Transactional(readOnly = true)
    public List<CartItemReadResponse> readAll(User user) {
        // 장바구니 상품 조회
        List<CartItem> cartItems = cartItemRepository.findAllByUserId(user.getId());

        // todo: N+1 문제 발생
        return cartItems.stream()
            .map(cartItem -> {
                // todo: getProduct().getId()로 인해 같은 쿼리 2번 나갈거라고 예상
                Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("유효하지 않은 상품 번호입니다."));

                // todo: 여기서도 N+1 발생
                // 대표 이미지 가져오기 (한 개만)
                String thumbnailImageUrl = productImageRepository.findByThumbnailImage(
                        product.getId())
                    .getImageUrl();

                return CartItemReadResponse.from(cartItem, product, thumbnailImageUrl);
            })
            .toList();
    }

    @Transactional
    public CartItem addCartItem(User user, CartItemAddRequest request) {

        // 상품이 있는지 없는지 체크
        Product product = productRepository.findById(request.productId())
            .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "product"));

        // 현재 장바구니에 있는지 체크
        Optional<CartItem> optionalCartItem = cartItemRepository.findByUserIdAndProductId(user.getId(), product.getId());

        // 도메인 클래스에서 비즈니스 로직 다룸
        CartItem cartItem = CartItem.add(user, product, optionalCartItem.orElse(null));

        return cartItemRepository.save(cartItem);
    }


    // todo : 감소한 결과가 0이 되면 안된다는 로직 추가할 것
    @Transactional
    public CartItem changeCartItemQuantity(Long userId, Long cartId, CartItemQuantityChangeRequest request) {



        CartItem cartItem = cartItemRepository.findById(cartId)
            .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "cart item"));
        if (!Objects.equals(cartItem.getUser().getId(), userId)) {
            throw new CommonException(BError.FAIL_FOR_REASON,
                "change cart item qunatity",
                "user doesn't have permission to change cart item");
        }

        return cartItemRepository.save(cartItem.changeQuantity(request.quantity()));
    }

    @Transactional(readOnly = true)
    public List<CartItem> retrieveCartItems(Long userId) {
        return cartItemRepository.findAllByUserId(userId);
    }

    @Transactional
    public Long getTotalCount(Long userId) {
        return cartItemRepository.countByUserId(userId);
    }

    @Transactional
    public void deleteCartItem(Long userId, Long cartItemId) {

        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "cart item"));
        if (!Objects.equals(cartItem.getUser().getId(), userId)) {
            throw new CommonException(BError.FAIL_FOR_REASON,
                "delete cart item",
                "user doesn't have permission to delete cart item");
        }
        cartItemRepository.save(cartItem.remove());
    }
}
