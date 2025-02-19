package com.sellect.server.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sellect.server.auth.domain.User;
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
    private final OrderService sut = new OrderService(ordersRepository, orderItemRepository,
        productRepository, cartRepository, userReceivedCouponRepository);
    private User user;

    @BeforeEach
    void setUp() {
        ordersRepository.clear();
        orderItemRepository.clear();
        productRepository.clear();
        cartRepository.clear();
        userReceivedCouponRepository.clear();
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
                .build());

            productRepository.save(Product.builder()
                .id(2L)
                .build());
            userReceivedCouponRepository.save(UserReceivedCoupon.builder()
                .id(1L)
                .build());

            OrderAddRequest request = new OrderAddRequest(
                1L,
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
            //assertThat(savedOrder.getUserReceivedCoupon().getId()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("상품 락 테스트")
    class LockProductItemsTest {

        Product product1 = Product.builder()
            .id(1L)
            .stock(5)
            .build();

        @Test
        @DisplayName("재고가 부족 시, 예외 발생")
        void testCheckStock() {
            // Given
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
                () -> sut.LockProductItems(savedOrder.getId()));
            assertEquals("재고 부족 is not valid", exception.getMessage());
        }

//        @Test
//        @DisplayName("Lock 시 읽기 가능, 수정 불가능 성공")
//        void testLock() throws InterruptedException {
//            // Given
//            Orders savedOrder = ordersRepository.save(Orders.builder()
//                .id(1L)
//                .build());
//
//            List<OrderItem> savedOrderItems = orderItemRepository.saveAll(List.of(
//                OrderItem.builder()
//                    .id(1L)
//                    .orders(savedOrder)
//                    .product(product1)
//                    .build()
//            ));
//
//            // When
//            // 락을 걸기 위한 객체
//            Object lock = new Object();
//
//            // 읽기 성공 여부를 확인하기 위한 AtomicBoolean 변수
//            AtomicBoolean isReadSuccessful = new AtomicBoolean(false);
//
//            // 스레드 A: LockProductItems 호출하여 락 획득
//            Thread threadA = new Thread(() -> {
//                try {
//                    // 락을 획득
//                    sut.LockProductItems(savedOrder.getId());
//
//                    synchronized (lock) {
//                        lock.notifyAll();  // 락을 획득했음을 알려줌
//                    }
//                    Thread.sleep(2000);  // 2초 동안 락을 유지한다고 가정
//                } catch (Exception e) {
//                    System.out.println("락 획득 실패: " + e.getMessage());
//                }
//            });
//            threadA.start();
//
//            // 스레드 B: 락이 걸린 동안 상품 읽기 시도
//            Thread threadB = new Thread(() -> {
//                try {
//                    synchronized (lock) {
//                        lock.wait();  // threadA가 락을 획득할 때까지 기다림
//                    }
//
//                    Optional<Product> readProduct = productRepository.findById(product1.getId());
//                    readProduct.ifPresent(p -> {
//                        isReadSuccessful.set(true);
//                        System.out.println("읽기 성공: " + p.getStock());
//                    });
//                } catch (Exception e) {
//                    System.out.println("읽기 실패: " + e.getMessage());
//                }
//            });
//
//            // 스레드 C: 락이 걸린 동안 상품 수정 시도
//            Thread threadC = new Thread(() -> {
//                try {
//                    synchronized (lock) {
//                        lock.wait();  // threadA가 락을 획득할 때까지 기다림
//                    }
//                    // 수정 시도
//                    productRepository.save(product1.updateStock(3));
//                } catch (Exception e) {
//                    System.out.println("수정 실패(락 활성화): " + e.getMessage());
//                }
//            });
//
//            threadB.start();
//            threadC.start();
//
//            // threadA가 락을 획득한 후, threadB와 threadC가 시작되도록 대기
//            threadA.join();
//            threadB.join();
//            threadC.join();
//
//            // Then
//            // 1. 읽기 성공 여부 검증
//            assertTrue(isReadSuccessful.get());
//
//            // 2. 상품이 수정되지 않았는지 확인
//            Optional<Product> lockedProduct = productRepository.findById(product1.getId());
//            assertThat(lockedProduct).isPresent();
//            assertThat(lockedProduct.get().getStock()).isEqualTo(5); // 원래의 재고 값 (5)
//        }
    }

    @Nested
    @DisplayName("주문 완료 테스트")
    class CompleteOrderTest {

        Product product1 = Product.builder()
            .id(1L)
            .name("상품1")
            .price(new BigDecimal("10000"))
            .stock(10)
            .build();
        UserReceivedCoupon userReceivedCoupon = UserReceivedCoupon.builder()
            .id(1L)
            .build();

        @Test
        @DisplayName("재고 차감 & 주문 complete 상태 변경 성공, "
            + "장바구니 비우기 & 쿠폰 사용 처리 성공")
        void testCompleteOrder() {
            // Given
            Orders savedOrder = ordersRepository.save(Orders.builder()
                .id(1L)
                .userReceivedCoupon(userReceivedCoupon)
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
            sut.completeOrder(user, savedOrder.getId());

            // Then
            Product updatedProduct = productRepository.findById(1L).orElseThrow();
            assertThat(updatedProduct.getStock()).isEqualTo(5); // 재고가 차감되었는지 확인
        }

        @Test
        @DisplayName("장바구니 비우기 & 쿠폰 사용 처리 성공")
        void testClearCartCoupon() {
            // Given
            Orders savedOrder = ordersRepository.save(Orders.builder()
                .id(1L)
                .userReceivedCoupon(userReceivedCoupon)
                .status(OrderStatus.COMPLETED)
                .build());

            // When
            sut.clearCartAndDeleteCoupons(user, savedOrder);

            // Then
            assertThat(cartRepository.findAllByUserId(user.getId())).isEmpty();
            assertThat(userReceivedCoupon.getIsUsed()).isTrue();
        }
    }


    @Nested
    @DisplayName("주문 조회 테스트")
    class GetOrdersTest {

        Product product1 = Product.builder()
            .id(1L)
            .name("상품1")
            .stock(10)
            .build();

        Product product2 = Product.builder()
            .id(2L)
            .name("상품2")
            .stock(5)
            .build();
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
            Orders savedOrder = ordersRepository.save(Orders.builder()
                .id(1L)
                .user(user)
                .status(OrderStatus.COMPLETED)
                .build());
            orderItemRepository.saveAll(List.of(
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
                .userReceivedCoupon(userReceivedCoupon)
                .status(OrderStatus.COMPLETED)
                .totalPrice(new BigDecimal("50000"))
                .build());
            orderItemRepository.saveAll(List.of(
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
            assertThat(orderDetail.discountCost()).isEqualTo(new BigDecimal("10000"));
        }
    }
}
