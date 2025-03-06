package com.sellect.server.order.repository;

import static org.assertj.core.api.BDDAssertions.then;

import com.sellect.server.auth.domain.User;
import com.sellect.server.auth.repository.entity.Role;
import com.sellect.server.auth.repository.entity.UserEntity;
import com.sellect.server.config.JpaConfig;
import com.sellect.server.config.JsonConfig;
import com.sellect.server.order.domain.Orders;
import com.sellect.server.order.repository.entity.OrderStatus;
import java.math.BigDecimal;
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
class OrdersRepositoryImplTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private OrdersJpaRepository ordersJpaRepository;

    private OrdersRepositoryImpl ordersRepository;
    private UserEntity userEntity;
    private User user;

    @BeforeEach
    void setUp() {
        ordersRepository = new OrdersRepositoryImpl(ordersJpaRepository);

        userEntity = UserEntity.builder()
            .uuid("test-uuid")
            .nickname("Test User")
            .role(Role.USER)
            .build();
        em.persist(userEntity);
        em.flush();

        user = userEntity.toModel();

        em.clear();
    }

    @Nested
    @DisplayName("save()")
    class OrderSaveTest {

        @Test
        @DisplayName("새로 생성된 order를 저장한다.")
        void saveNewOrder() {
            // given
            Orders order = Orders.register(user, new BigDecimal("100000"), OrderStatus.PENDING);

            // when
            Orders savedOrders = ordersRepository.save(order);

            // then
            then(savedOrders).isNotNull();
            then(savedOrders.getId()).isNotNull();
            then(savedOrders.getUser().getId()).isEqualTo(userEntity.getId());
            then(savedOrders.getUserReceivedCoupon()).isNull();
            then(savedOrders.getOrderNumber()).isNotNull();
            then(savedOrders.getTotalPrice()).isEqualTo(new BigDecimal("100000"));
            then(savedOrders.getStatus()).isEqualTo(OrderStatus.PENDING);
            then(savedOrders.getCreatedAt()).isNotNull();
            then(savedOrders.getUpdatedAt()).isNotNull();
            then(savedOrders.getCreatedAt()).isEqualTo(savedOrders.getUpdatedAt());
            then(savedOrders.getDeletedAt()).isNull();
        }
    }
}
