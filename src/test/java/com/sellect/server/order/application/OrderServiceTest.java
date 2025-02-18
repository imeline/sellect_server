package com.sellect.server.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sellect.server.auth.domain.User;
import com.sellect.server.common.exception.CommonException;
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
    private final OrderService sut = new OrderService(ordersRepository, orderItemRepository,
        productRepository);
    private User user;

    @BeforeEach
    void setUp() {
        ordersRepository.clear();
        orderItemRepository.clear();
        productRepository.clear();
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
            Product product1 = productRepository.save(Product.builder()
                .id(1L)
                .build());

            Product product2 = productRepository.save(Product.builder()
                .id(2L)
                .build());

            OrderAddRequest request = new OrderAddRequest(
                "200000",
                List.of(
                    new OrderItemAddRequest(1L, "10000", 10),
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
    }

    @Nested
    @DisplayName("상품 락 테스트")
    class LockProductItemsTest {

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

            List<OrderItem> savedOrderItems = orderItemRepository.saveAll(List.of(
                OrderItem.builder()
                    .id(1L)
                    .orders(savedOrder)
                    .product(product1)
                    .quantity(10)
                    .build()
            ));

            // When & Then
            CommonException exception = assertThrows(CommonException.class,
                () -> sut.LockProductItems(savedOrder.getId()));
            assertEquals("재고 부족 is not valid", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("주문 완료 테스트")
    class CompleteOrderTest {

        Product product1 = productRepository.save(Product.builder()
            .id(1L)
            .name("상품1")
            .price(new BigDecimal("10000"))
            .stock(10)
            .build());

        @Test
        @DisplayName("재고 차감 & 주문 complete 상태 변경 성공")
        void testCompleteOrder() {
            // Given
            Orders savedOrder = ordersRepository.save(Orders.builder()
                .id(1L)
                .status(OrderStatus.PENDING)
                .build());

            List<OrderItem> savedOrderItems = orderItemRepository.saveAll(List.of(
                OrderItem.builder()
                    .id(1L)
                    .orders(savedOrder)
                    .product(product1)
                    .quantity(5)
                    .build()
            ));

            // When
            sut.completeOrder(user, savedOrder.getId());

            // Then
            Product updatedProduct = productRepository.findById(1L).orElseThrow();
            assertThat(updatedProduct.getStock()).isEqualTo(5); // 재고가 차감되었는지 확인
        }
    }

    @Nested
    @DisplayName("주문 조회 테스트")
    class GetOrdersTest {

        Product product1 = productRepository.save(Product.builder()
            .id(1L)
            .name("상품1")
            .stock(10)
            .build());

        Product product2 = productRepository.save(Product.builder()
            .id(2L)
            .name("상품2")
            .stock(5)
            .build());

        @Test
        @DisplayName("사용자의 모든 주문을 조회 성공")
        void testGetOrdersByUser() {
            // Given
            Orders savedOrder = ordersRepository.save(Orders.builder()
                .id(1L)
                .user(user)
                .status(OrderStatus.COMPLETED)
                .build());
            List<OrderItem> savedOrderItems = orderItemRepository.saveAll(List.of(
                OrderItem.builder()
                    .id(1L)
                    .orders(savedOrder)
                    .product(product1)
                    .price(new BigDecimal("10000"))
                    .quantity(10)
                    .build(),
                OrderItem.builder()
                    .id(2L)
                    .orders(savedOrder)
                    .product(product2)
                    .price(new BigDecimal("20000"))
                    .quantity(5)
                    .build()
            ));
            // When
            List<OrderGetResponse> orderResponses = sut.getOrdersByUser(user);

            // Then
            assertThat(orderResponses.get(0).orderItems()).hasSize(2); // 모든 주문 아이템이 조회 됐는지 체크
            assertThat(orderResponses.get(0).orderId()).isEqualTo(
                savedOrder.getId());  // orderId 반환값 체크
        }

        @Test
        @DisplayName("주문 상세 정보 조회 성공")
        void testGetOrderDetail() {
            // Given
            Orders savedOrder = ordersRepository.save(Orders.builder()
                .id(1L)
                .status(OrderStatus.COMPLETED)
                .totalPrice(new BigDecimal("50000"))
                .build());
            List<OrderItem> savedOrderItems = orderItemRepository.saveAll(List.of(
                OrderItem.builder()
                    .id(1L)
                    .orders(savedOrder)
                    .product(product1)
                    .price(new BigDecimal("10000"))
                    .quantity(10)
                    .build()
            ));

            sut.completeOrder(user, savedOrder.getId());

            // When
            OrderDetailGetResponse orderDetail = sut.getOrderDetail(savedOrder.getId());

            // Then
            assertThat(orderDetail.orderItems()).hasSize(1);
            assertThat(orderDetail.orderItems().get(0).productName()).isEqualTo("상품1");
        }
    }
}
