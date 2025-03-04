package com.sellect.server.order.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sellect.server.auth.repository.entity.Role;
import com.sellect.server.auth.repository.entity.UserEntity;
import com.sellect.server.auth.repository.user.UserJpaRepository;
import com.sellect.server.order.repository.entity.OrderStatus;
import com.sellect.server.order.repository.entity.OrdersEntity;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
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

// JPA 관련 Bean만 로드
@DataJpaTest
// @Container와 @ServiceConnection을 사용해 MySQLContainer를 클래스 단위에서 실행
@Testcontainers
// Spring의 DI 기능을 JUnit 5 테스트에서 사용할 수 있도록 해주는 역할
@ExtendWith(SpringExtension.class)
// 테스트 클래스 전체에서 하나의 인스턴스를 사용
// but, 테스트 메소드 간에 독립성 깨짐. 의존성이 없어야 함
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrdersJpaRepositoryTest {

    @Container
    @ServiceConnection
    private static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("test-db")
        .withUsername("test")
        .withPassword("test");

    @Autowired
    private OrdersJpaRepository ordersJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    // 테스트에서 여러 번 재사용할 UserEntity 객체를 미리 저장
    // UserEntity가 먼저 저장되어야 OrdersEntity를 저장 가능
    private UserEntity testUser;

    @BeforeEach
    void setup() {
        testUser = userJpaRepository.save(UserEntity.builder()
            .uuid("test-uuid")
            .nickname("test")
            .role(Role.USER)
            .build());
    }

    @Test
    void testFindCompletedOrdersByUser() {
        // given
        ordersJpaRepository.save(OrdersEntity.builder()
            .userEntity(testUser)
            .totalPrice(BigDecimal.valueOf(1000))
            .orderNumber("ORD123")
            .status(OrderStatus.COMPLETED)
            .build());

        // when
        List<OrdersEntity> completedOrders = ordersJpaRepository.findCompletedOrdersByUser(
            testUser, OrderStatus.COMPLETED);

        // then
        assertThat(completedOrders).hasSize(1);
        assertThat(completedOrders.get(0).getOrderNumber()).isEqualTo("ORD123");
    }
    
    @Test
    void testSaveAndFind() {
        // given
        OrdersEntity order = OrdersEntity.builder()
            .userEntity(testUser)
            .totalPrice(BigDecimal.valueOf(1000))
            .orderNumber("ORD456")
            .status(OrderStatus.PENDING)
            .build();

        // when
        OrdersEntity savedOrder = ordersJpaRepository.save(order);

        Optional<OrdersEntity> foundOrder = ordersJpaRepository.findById(savedOrder.getId());

        // then
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getOrderNumber()).isEqualTo("ORD456");
    }

}

