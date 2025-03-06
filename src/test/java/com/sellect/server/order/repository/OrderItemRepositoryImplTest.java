package com.sellect.server.order.repository;

import static org.assertj.core.api.BDDAssertions.then;

import com.sellect.server.auth.repository.entity.Role;
import com.sellect.server.auth.repository.entity.UserEntity;
import com.sellect.server.brand.repository.BrandEntity;
import com.sellect.server.category.repository.CategoryEntity;
import com.sellect.server.config.JpaConfig;
import com.sellect.server.config.JsonConfig;
import com.sellect.server.order.domain.OrderItem;
import com.sellect.server.order.domain.Orders;
import com.sellect.server.order.repository.entity.OrderItemEntity;
import com.sellect.server.order.repository.entity.OrderStatus;
import com.sellect.server.order.repository.entity.OrdersEntity;
import com.sellect.server.product.domain.Product;
import com.sellect.server.product.repository.ProductEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({
    JpaConfig.class,
    JsonConfig.class,
})
class OrderItemRepositoryImplTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private OrderItemJpaRepository orderItemJpaRepository;

    private OrderItemRepositoryImpl orderItemRepository;

    private UserEntity userEntity;
    private OrdersEntity ordersEntity;
    private Orders orders;
    private CategoryEntity categoryEntity;
    private BrandEntity brandEntity;
    private ProductEntity productEntity;
    private Product product;

    @BeforeEach
    void setUp() {
        orderItemRepository = new OrderItemRepositoryImpl(orderItemJpaRepository);
        em.clear();

        userEntity = UserEntity.builder()
            .uuid("test-uuid-" + System.currentTimeMillis()) // 테스트마다 고유한 UUID 생성
            .nickname("Test User")
            .role(Role.USER)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        em.persist(userEntity);
        em.flush();

        ordersEntity = OrdersEntity.builder()
            .userEntity(userEntity)
            .totalPrice(new BigDecimal("100000"))
            .orderNumber("ORDER-1")
            .status(OrderStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        em.persist(ordersEntity);
        em.flush();

        orders = ordersEntity.toModel();

        UserEntity sellerEntity = UserEntity.builder()
            .uuid("test-uuid-" + System.currentTimeMillis()) // 테스트마다 고유한 UUID 생성
            .nickname("Test Seller")
            .role(Role.SELLER)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        em.persist(sellerEntity);
        em.flush();

        categoryEntity = CategoryEntity.builder()
            .name("Test Category")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        em.persist(categoryEntity);
        em.flush();

        brandEntity = BrandEntity.builder()
            .name("Test Brand")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        em.persist(brandEntity);
        em.flush();

        productEntity = ProductEntity.builder()
            .name("Test Product")
            .sellerEntity(sellerEntity)
            .categoryEntity(categoryEntity)
            .brandEntity(brandEntity)
            .price(new BigDecimal("50000"))
            .description("Test Description")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        em.persist(productEntity);
        em.flush();

        product = productEntity.toModel();

        em.clear();
    }

    @Nested
    @DisplayName("saveAll()")
    class OrderItemSaveAllTest {

        @Test
        @DisplayName("새로운 orderItem 리스트를 저장한다.")
        void saveNewOrderItems() {
            // given
            OrderItem orderItem1 = OrderItem.register(orders, product, new BigDecimal("50000"), 1);
            OrderItem orderItem2 = OrderItem.register(orders, product, new BigDecimal("50000"), 1);

            // when
            List<OrderItem> savedOrderItems = orderItemRepository.saveAll(
                List.of(orderItem1, orderItem2));

            // then
            then(savedOrderItems).isNotNull();
            then(savedOrderItems).hasSize(2);
            then(savedOrderItems.get(0).getOrders().getId()).isEqualTo(ordersEntity.getId());
            then(savedOrderItems.get(0).getProduct().getId()).isEqualTo(productEntity.getId());
            then(savedOrderItems.get(0).getPrice()).isEqualTo(new BigDecimal("50000"));
            then(savedOrderItems.get(0).getQuantity()).isEqualTo(1);
            then(savedOrderItems.get(1).getOrders().getId()).isEqualTo(ordersEntity.getId());
            then(savedOrderItems.get(1).getProduct().getId()).isEqualTo(productEntity.getId());
            then(savedOrderItems.get(1).getPrice()).isEqualTo(new BigDecimal("50000"));
            then(savedOrderItems.get(1).getQuantity()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("findAllByOrdersId()")
    class FindAllByOrdersIdTest {

        @Test
        @DisplayName("ordersId로 orderItem 리스트를 조회한다.")
        void willSuccess() {
            // given
            OrderItemEntity orderItemEntity1 = OrderItemEntity.builder()
                .ordersEntity(ordersEntity)
                .productEntity(productEntity)
                .price(new BigDecimal("50000"))
                .quantity(1)
                .createdAt(LocalDateTime.now())
                .build();
            OrderItemEntity orderItemEntity2 = OrderItemEntity.builder()
                .ordersEntity(ordersEntity)
                .productEntity(productEntity)
                .price(new BigDecimal("50000"))
                .quantity(1)
                .createdAt(LocalDateTime.now())
                .build();
            orderItemJpaRepository.save(orderItemEntity1);
            orderItemJpaRepository.save(orderItemEntity2);

            // when
            List<OrderItem> foundOrderItems = orderItemRepository.findAllByOrdersId(
                ordersEntity.getId());

            // then
            then(foundOrderItems).isNotNull();
            then(foundOrderItems).hasSize(2);
            then(foundOrderItems.get(0).getOrders().getId()).isEqualTo(ordersEntity.getId());
            then(foundOrderItems.get(0).getProduct().getId()).isEqualTo(productEntity.getId());
            then(foundOrderItems.get(0).getPrice()).isEqualTo(new BigDecimal("50000"));
            then(foundOrderItems.get(0).getQuantity()).isEqualTo(1);
            then(foundOrderItems.get(1).getOrders().getId()).isEqualTo(ordersEntity.getId());
            then(foundOrderItems.get(1).getProduct().getId()).isEqualTo(productEntity.getId());
            then(foundOrderItems.get(1).getPrice()).isEqualTo(new BigDecimal("50000"));
            then(foundOrderItems.get(1).getQuantity()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("calculateSalesByProductId()")
    class CalculateSalesByProductIdTest {

        @Test
        @DisplayName("특정 productId로 완료된 주문의 총 매출을 계산한다.")
        void willSuccess() {
            // given
            OrdersEntity ordersEntity1 = OrdersEntity.builder()
                .userEntity(userEntity)
                .totalPrice(new BigDecimal("50000"))
                .orderNumber("ORDER-2")
                .status(OrderStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            OrdersEntity ordersEntity2 = OrdersEntity.builder()
                .userEntity(userEntity)
                .totalPrice(new BigDecimal("50000"))
                .orderNumber("ORDER-3")
                .status(OrderStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            OrdersEntity ordersEntity3 = OrdersEntity.builder()
                .userEntity(userEntity)
                .totalPrice(new BigDecimal("30000"))
                .orderNumber("ORDER-4")
                .status(OrderStatus.PENDING) // PENDING 주문은 제외
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            em.persist(ordersEntity1);
            em.persist(ordersEntity2);
            em.persist(ordersEntity3);
            em.flush();

            OrderItemEntity orderItemEntity1 = OrderItemEntity.builder()
                .ordersEntity(ordersEntity1)
                .productEntity(productEntity)
                .price(new BigDecimal("50000"))
                .quantity(1)
                .createdAt(LocalDateTime.now()) // createdAt 설정
                .build();
            OrderItemEntity orderItemEntity2 = OrderItemEntity.builder()
                .ordersEntity(ordersEntity2)
                .productEntity(productEntity)
                .price(new BigDecimal("50000"))
                .quantity(1)
                .createdAt(LocalDateTime.now()) // createdAt 설정
                .build();
            OrderItemEntity orderItemEntity3 = OrderItemEntity.builder()
                .ordersEntity(ordersEntity3)
                .productEntity(productEntity)
                .price(new BigDecimal("30000"))
                .quantity(1)
                .createdAt(LocalDateTime.now()) // createdAt 설정
                .build();
            orderItemJpaRepository.save(orderItemEntity1);
            orderItemJpaRepository.save(orderItemEntity2);
            orderItemJpaRepository.save(orderItemEntity3);

            // when
            Optional<BigDecimal> sales = orderItemRepository.calculateSalesByProductId(
                productEntity.getId());

            // then
            then(sales).isPresent();
            // db 에서 가져온 BigDecimal 은 소수점이 붙고, java에서 만든 BigDecimal은 소수점이 없음
            // compareTo 로 스케일과 표기법을 무시하고 값이 같은지 비교
            then(sales.get()
                .compareTo(new BigDecimal("100000"))).isZero(); // ORDER-2 + ORDER-3 (COMPLETED)
        }
    }

    @Nested
    @DisplayName("countCompleteOrdersByProductId()")
    class CountCompleteOrdersByProductIdTest {

        @Test
        @DisplayName("특정 productId로 완료된 주문 수를 계산한다.")
        void willSuccess() {
            // given
            OrdersEntity ordersEntity1 = OrdersEntity.builder()
                .userEntity(userEntity)
                .totalPrice(new BigDecimal("50000"))
                .orderNumber("ORDER-2")
                .status(OrderStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            OrdersEntity ordersEntity2 = OrdersEntity.builder()
                .userEntity(userEntity)
                .totalPrice(new BigDecimal("50000"))
                .orderNumber("ORDER-3")
                .status(OrderStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            OrdersEntity ordersEntity3 = OrdersEntity.builder()
                .userEntity(userEntity)
                .totalPrice(new BigDecimal("30000"))
                .orderNumber("ORDER-4")
                .status(OrderStatus.PENDING) // PENDING 주문은 제외
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            em.persist(ordersEntity1);
            em.persist(ordersEntity2);
            em.persist(ordersEntity3);
            em.flush();

            OrderItemEntity orderItemEntity1 = OrderItemEntity.builder()
                .ordersEntity(ordersEntity1)
                .productEntity(productEntity)
                .price(new BigDecimal("50000"))
                .quantity(1)
                .createdAt(LocalDateTime.now()) // createdAt 설정
                .build();
            OrderItemEntity orderItemEntity2 = OrderItemEntity.builder()
                .ordersEntity(ordersEntity2)
                .productEntity(productEntity)
                .price(new BigDecimal("50000"))
                .quantity(1)
                .createdAt(LocalDateTime.now()) // createdAt 설정
                .build();
            OrderItemEntity orderItemEntity3 = OrderItemEntity.builder()
                .ordersEntity(ordersEntity3)
                .productEntity(productEntity)
                .price(new BigDecimal("30000"))
                .quantity(1)
                .createdAt(LocalDateTime.now()) // createdAt 설정
                .build();
            orderItemJpaRepository.save(orderItemEntity1);
            orderItemJpaRepository.save(orderItemEntity2);
            orderItemJpaRepository.save(orderItemEntity3);

            // when
            Optional<Integer> count = orderItemRepository.countCompleteOrdersByProductId(
                productEntity.getId());

            // then
            then(count).isPresent();
            then(count.get()).isEqualTo(2); // ORDER-2, ORDER-3 (COMPLETED)
        }
    }

    @Nested
    @DisplayName("calculateTotalSalesByProductIds()")
    class CalculateTotalSalesByProductIdsTest {

        @Test
        @DisplayName("여러 productId로 완료된 주문의 총 매출을 계산한다.")
        void willSuccess() {
            // given
            ProductEntity productEntity2 = ProductEntity.builder()
                .name("Test Product 2")
                .price(new BigDecimal("30000"))
                .description("Test Description 2")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            em.persist(productEntity2);
            em.flush();

            OrdersEntity ordersEntity1 = OrdersEntity.builder()
                .userEntity(userEntity)
                .totalPrice(new BigDecimal("50000"))
                .orderNumber("ORDER-2")
                .status(OrderStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            OrdersEntity ordersEntity2 = OrdersEntity.builder()
                .userEntity(userEntity)
                .totalPrice(new BigDecimal("30000"))
                .orderNumber("ORDER-3")
                .status(OrderStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            OrdersEntity ordersEntity3 = OrdersEntity.builder()
                .userEntity(userEntity)
                .totalPrice(new BigDecimal("30000"))
                .orderNumber("ORDER-4")
                .status(OrderStatus.PENDING) // PENDING 주문은 제외
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            em.persist(ordersEntity1);
            em.persist(ordersEntity2);
            em.persist(ordersEntity3);
            em.flush();

            OrderItemEntity orderItemEntity1 = OrderItemEntity.builder()
                .ordersEntity(ordersEntity1)
                .productEntity(productEntity)
                .price(new BigDecimal("50000"))
                .quantity(1)
                .createdAt(LocalDateTime.now()) // createdAt 설정
                .build();
            OrderItemEntity orderItemEntity2 = OrderItemEntity.builder()
                .ordersEntity(ordersEntity2)
                .productEntity(productEntity2)
                .price(new BigDecimal("30000"))
                .quantity(1)
                .createdAt(LocalDateTime.now()) // createdAt 설정
                .build();
            OrderItemEntity orderItemEntity3 = OrderItemEntity.builder()
                .ordersEntity(ordersEntity3)
                .productEntity(productEntity2)
                .price(new BigDecimal("30000"))
                .quantity(1)
                .createdAt(LocalDateTime.now()) // createdAt 설정
                .build();
            orderItemJpaRepository.save(orderItemEntity1);
            orderItemJpaRepository.save(orderItemEntity2);
            orderItemJpaRepository.save(orderItemEntity3);

            // when
            Optional<BigDecimal> totalSales = orderItemRepository.calculateTotalSalesByProductIds(
                List.of(productEntity.getId(), productEntity2.getId()));

            // then
            then(totalSales).isPresent();
            then(totalSales.get()
                .compareTo(new BigDecimal("80000"))).isZero(); // ORDER-2 (50000) + ORDER-3 (30000)
        }
    }
}