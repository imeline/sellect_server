package com.sellect.server.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sellect.server.auth.domain.User;
import com.sellect.server.brand.domain.Brand;
import com.sellect.server.brand.repository.FakeBrandRepository;
import com.sellect.server.cart.domain.CartItem;
import com.sellect.server.cart.repository.FakeCartItemRepository;
import com.sellect.server.common.exception.CommonException;
import com.sellect.server.coupon.domain.Coupon;
import com.sellect.server.coupon.domain.UserReceivedCoupon;
import com.sellect.server.coupon.repository.FakeuserReceivedCouponRepository;
import com.sellect.server.order.controller.request.OrderAddRequest;
import com.sellect.server.order.controller.request.OrderItemAddRequest;
import com.sellect.server.order.controller.response.OrderDetailGetResponse;
import com.sellect.server.order.controller.response.OrderGetResponse;
import com.sellect.server.order.domain.OrderItem;
import com.sellect.server.order.domain.Orders;
import com.sellect.server.order.repository.FakeOrderItemRepository;
import com.sellect.server.order.repository.FakeOrdersRepository;
import com.sellect.server.order.repository.entity.OrderStatus;
import com.sellect.server.product.domain.Product;
import com.sellect.server.product.domain.ProductImage;
import com.sellect.server.product.repository.FakeProductImageRepository;
import com.sellect.server.product.repository.FakeProductRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OrderServiceTest {

    private final FakeOrdersRepository ordersRepository = new FakeOrdersRepository();
    private final FakeOrderItemRepository orderItemRepository = new FakeOrderItemRepository();
    private final FakeProductRepository productRepository = new FakeProductRepository();
    private final FakeCartItemRepository cartRepository = new FakeCartItemRepository();
    private final FakeuserReceivedCouponRepository userReceivedCouponRepository = new FakeuserReceivedCouponRepository();
    private final FakeProductImageRepository productImageRepository = new FakeProductImageRepository();
    private final FakeBrandRepository brandRepository = new FakeBrandRepository();
    private final OrderService sut = new OrderService(ordersRepository, orderItemRepository,
        productRepository, cartRepository, userReceivedCouponRepository, productImageRepository);
    private User user;

    @BeforeEach
    void setUp() {
        ordersRepository.clear();
        orderItemRepository.clear();
        productRepository.clear();
        cartRepository.clear();
        userReceivedCouponRepository.clear();
        brandRepository.clear();
        user = User.builder()
            .id(1L)
            .build();
    }

    @Nested
    @DisplayName("주문 등록 테스트")
    class RegisterPendingOrderTest {

        @Test
        @DisplayName("주문, 주문 아이템 생성 성공")
        void testRegisterPendingOrder() {
            // Given
            productRepository.save(Product.builder()
                .id(1L)
                .stock(50)
                .build());

            productRepository.save(Product.builder()
                .id(2L)
                .stock(50)
                .build());

            OrderAddRequest request = new OrderAddRequest(
                "200000",
                List.of(
                    new OrderItemAddRequest(1L, "10000", 5),
                    new OrderItemAddRequest(2L, "20000", 5)
                )
            );

            // When
            Orders savedOrder = sut.registerPendingOrder(user, request);

            // Then
            assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
            List<OrderItem> orderItems = orderItemRepository.findAllByOrdersId(savedOrder.getId());
            assertThat(orderItems).hasSize(2);
        }

        @Test
        @DisplayName("재고 부족 시, 에러 발생")
        void testStockError() {
            // Given
            productRepository.save(Product.builder()
                .id(1L)
                .stock(5)
                .build());

            OrderAddRequest request = new OrderAddRequest(
                "200000",
                List.of(
                    new OrderItemAddRequest(1L, "10000", 10)
                )
            );

            // When
            CommonException exception = assertThrows(CommonException.class, () ->
                sut.registerPendingOrder(user, request));
            // Then
            assertEquals("재고 부족 is not valid", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("주문 완료 테스트")
    class CompleteOrderTest {

        @Test
        @DisplayName("재고가 부족 시, 예외 발생")
        void testCheckStock() {
            // Given
            Product product1 = productRepository.save(Product.builder()
                .id(1L)
                .stock(5)
                .build());

            Orders savedOrder = ordersRepository.save(Orders.builder()
                .id(1L)
                .build());

            orderItemRepository.saveAll(List.of(
                OrderItem.builder()
                    .id(1L)
                    .orders(savedOrder)
                    .product(product1)
                    .quantity(10)
                    .build()
            ));

            // When & Then
            CommonException exception = assertThrows(CommonException.class,
                () -> sut.lockAndCompleteOrder(user, savedOrder.getId()));
            assertEquals("재고 부족 is not valid", exception.getMessage());
        }

        @Test
        @DisplayName("재고 차감 & 주문 complete 상태 변경 성공")
        void testCompleteOrder() {
            // Given
            Product product1 = productRepository.save(Product.builder()
                .id(1L)
                .stock(10)
                .build());

            Orders savedOrder = ordersRepository.save(Orders.builder()
                .id(1L)
                .status(OrderStatus.PENDING)
                .build());

            orderItemRepository.saveAll(List.of(
                OrderItem.builder()
                    .id(1L)
                    .orders(savedOrder)
                    .product(product1)
                    .quantity(5)
                    .build()
            ));

            // When
            Orders orders = sut.lockAndCompleteOrder(user, savedOrder.getId());

            // Then
            Product updatedProduct = productRepository.findById(1L).orElseThrow();
            assertThat(updatedProduct.getStock()).isEqualTo(5); // 재고가 차감되었는지 확인
            assertThat(orders.getStatus()).isEqualTo(
                OrderStatus.COMPLETED); // 주문 상태가 COMPLETED로 변경되었는지 확인
        }

        @Test
        @DisplayName("장바구니 비우기 & 쿠폰 사용 처리 성공")
        void testClearCartCoupon() {
            // Given
            cartRepository.save(CartItem.builder()
                .id(1L)
                .user(user)
                .build());

            cartRepository.save(CartItem.builder()
                .id(2L)
                .user(user)
                .build());

            userReceivedCouponRepository.save(UserReceivedCoupon.builder()
                .id(1L)
                .user(user)
                .isUsed(false)
                .build());
            UserReceivedCoupon userCoupon = userReceivedCouponRepository.findById(1L).orElseThrow();

            Orders savedOrder = ordersRepository.save(Orders.builder()
                .id(1L)
                .user(user)
                .userReceivedCoupon(userCoupon)
                .status(OrderStatus.COMPLETED)
                .build());

            // When
            sut.clearCartAndDeleteCoupon(user, savedOrder);

            // Then
            CartItem cart1 = cartRepository.findById(1L).orElseThrow();
            CartItem cart2 = cartRepository.findById(2L).orElseThrow();
            assertThat(cart1.getDeleteAt()).isNotNull();
            assertThat(cart2.getDeleteAt()).isNotNull();

            UserReceivedCoupon updatedCoupon = userReceivedCouponRepository.findById(1L)
                .orElseThrow();
            assertThat(updatedCoupon.getIsUsed()).isTrue();
        }
    }

    @Nested
    @DisplayName("주문 조회 테스트")
    class GetOrdersTest {

        Coupon coupon = Coupon.builder()
            .id(1L)
            .discountCost(10000)
            .build();
        UserReceivedCoupon userReceivedCoupon = UserReceivedCoupon.builder()
            .id(1L)
            .coupon(coupon)
            .build();

        @Test
        @DisplayName("사용자의 모든 주문을 조회 성공")
        void testGetOrdersByUser() {
            // Given
            Orders savedOrder1 = ordersRepository.save(Orders.builder()
                .id(1L)
                .user(user)
                .status(OrderStatus.COMPLETED)
                .build());
            Orders savedOrder2 = ordersRepository.save(Orders.builder()
                .id(2L)
                .user(user)
                .status(OrderStatus.COMPLETED)
                .build());
            Orders pendingOrder = ordersRepository.save(Orders.builder()
                .id(3L)
                .user(user)
                .status(OrderStatus.PENDING)
                .build());

            // When
            List<OrderGetResponse> orderResponses = sut.getOrdersByUser(user);

            // Then
            assertThat(orderResponses.size()).isEqualTo(2); // 확정 주문만 모두 조회 됐는지 체크
        }

        @Test
        @DisplayName("주문 상세 정보 조회 성공")
        void testGetOrderDetail() {
            // Given
            Brand brand = brandRepository.save(Brand.builder()
                .id(1L)
                .name("브랜드1")
                .build());

            Product product1 = productRepository.save(Product.builder()
                .id(1L)
                .name("상품1")
                .brand(brand)
                .build());

            productImageRepository.save(ProductImage.builder()
                .id(1L)
                .sequence(1)
                .product(product1)
                .representative(true)
                .imageUrl("image1.url")
                .build(), product1);

            Orders savedOrder = ordersRepository.save(Orders.builder()
                .id(1L)
                .status(OrderStatus.COMPLETED)
                .totalPrice(new BigDecimal("50000"))
                .build());

            orderItemRepository.saveAll(List.of(
                OrderItem.builder()
                    .id(1L)
                    .orders(savedOrder)
                    .product(product1)
                    .build()
            ));

            // When
            OrderDetailGetResponse orderDetail = sut.getOrderDetail(savedOrder.getId());

            // Then
            assertThat(orderDetail.orderItems()).hasSize(1);
            assertThat(orderDetail.orderItems().get(0).productName()).isEqualTo("상품1");
            assertThat(orderDetail.totalPrice()).isEqualTo(new BigDecimal("50000"));
            assertEquals("브랜드1", orderDetail.orderItems().get(0).brandName());
            assertEquals("image1.url", orderDetail.orderItems().get(0).imageUrl());
        }
    }
}
