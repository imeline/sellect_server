package com.sellect.server.order.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sellect.server.auth.repository.entity.Role;
import com.sellect.server.auth.repository.entity.UserEntity;
import com.sellect.server.auth.repository.user.UserJpaRepository;
import com.sellect.server.order.repository.entity.OrderItemEntity;
import com.sellect.server.order.repository.entity.OrderStatus;
import com.sellect.server.order.repository.entity.OrdersEntity;
import com.sellect.server.product.repository.ProductEntity;
import com.sellect.server.product.repository.ProductJpaRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Testcontainers
@ExtendWith(SpringExtension.class)
class OrderItemJpaRepositoryTest {

    @Container
    @ServiceConnection
    private static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("test-db")
        .withUsername("test")
        .withPassword("test");

    @Autowired
    private OrderItemJpaRepository orderItemJpaRepository;

    @Autowired
    private OrdersJpaRepository ordersJpaRepository;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    private OrdersEntity testOrder;
    private ProductEntity testProduct;
    private LocalDateTime time;

    @BeforeEach
    void setup() {
        UserEntity testUser = userJpaRepository.save(UserEntity.builder()
            .uuid("test-uuid")
            .nickname("test")
            .role(Role.USER)
            .build());

        testOrder = ordersJpaRepository.save(OrdersEntity.builder()
            .userEntity(testUser)
            .totalPrice(BigDecimal.valueOf(5000))
            .orderNumber("ORD-999")
            .status(OrderStatus.PENDING)
            .build());

        testProduct = productJpaRepository.save(ProductEntity.builder()
            .name("Test Product")
            .price(BigDecimal.valueOf(1000))
            .build());

        time = LocalDateTime.parse("2024-08-01T00:00:00");
    }

    @Test
    void testFindAllByOrdersEntityId() {
        // given
        OrderItemEntity orderItem1 = orderItemJpaRepository.save(OrderItemEntity.builder()
            .ordersEntity(testOrder)
            .productEntity(testProduct)
            .price(BigDecimal.valueOf(1000))
            .quantity(2)
            .createdAt(time)
            .build());

        OrderItemEntity orderItem2 = orderItemJpaRepository.save(OrderItemEntity.builder()
            .ordersEntity(testOrder)
            .productEntity(testProduct)
            .price(BigDecimal.valueOf(2000))
            .quantity(1)
            .createdAt(time)
            .build());

        // when
        List<OrderItemEntity> orderItems = orderItemJpaRepository.findAllByOrdersEntityId(
            testOrder.getId());

        // then
        assertThat(orderItems).hasSize(2);
        assertThat(orderItems).extracting("id").contains(orderItem1.getId(), orderItem2.getId());
    }

    @Test
    void testSaveAndFind() {
        // given
        OrderItemEntity orderItem = OrderItemEntity.builder()
            .ordersEntity(testOrder)
            .productEntity(testProduct)
            .price(BigDecimal.valueOf(500))
            .quantity(2)
            .createdAt(time)
            .build();

        // when
        OrderItemEntity savedOrderItem = orderItemJpaRepository.save(orderItem);
        List<OrderItemEntity> foundItems = orderItemJpaRepository.findAllByOrdersEntityId(
            testOrder.getId());

        // then
        assertThat(foundItems).isNotEmpty();
        assertThat(foundItems.get(0).getId()).isEqualTo(savedOrderItem.getId());
        assertThat(foundItems.get(0).getQuantity()).isEqualTo(2);
    }
}
