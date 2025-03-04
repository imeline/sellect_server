package com.sellect.server.order.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.sellect.server.order.repository.entity.OrderStatus;
import com.sellect.server.product.domain.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OrderItemTest {

    private Orders orders;
    private Product product;
    private BigDecimal price;
    private int quantity;

    @BeforeEach
    void setUp() {

        orders = Orders.builder()
            .id(1L)
            .user(null) // User는 OrderItem 테스트에 필요 없으므로 null로 설정
            .totalPrice(new BigDecimal("100000"))
            .orderNumber("test-order-number")
            .status(OrderStatus.PENDING)
            .createdAt(LocalDateTime.parse("2024-08-01T00:00:00"))
            .updatedAt(LocalDateTime.parse("2024-08-01T00:00:00"))
            .build();

        // Product 객체 생성 (간단한 객체로 가정)
        product = Product.builder()
            .id(1L)
            .name("Test Product")
            .price(new BigDecimal("50000"))
            .build();

        price = new BigDecimal("50000");
        quantity = 2;
    }

    @Test
    void testRegister() {
        // when
        OrderItem orderItem = OrderItem.register(orders, product, price, quantity);

        // then
        assertNotNull(orderItem);
        assertEquals(orders, orderItem.getOrders());
        assertEquals(product, orderItem.getProduct());
        assertEquals(price, orderItem.getPrice());
        assertEquals(quantity, orderItem.getQuantity());
        assertNotNull(orderItem.getCreatedAt());
        assertNull(orderItem.getDeleteAt());
    }
}