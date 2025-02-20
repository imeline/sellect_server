package com.sellect.server.cart.repository;

import com.sellect.server.auth.repository.entity.UserEntity;
import com.sellect.server.cart.domain.CartItem;
import com.sellect.server.common.BaseTimeEntity;
import com.sellect.server.product.repository.ProductEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@Table(name = "cart_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CartItemEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private ProductEntity productEntity;

    private Integer quantity;

    public static CartItemEntity from(CartItem cartItem) {
        return CartItemEntity.builder()
            .id(cartItem.getId())
            .userEntity(UserEntity.from(cartItem.getUser()))
            .productEntity(ProductEntity.from(cartItem.getProduct()))
            .quantity(cartItem.getQuantity())
            .createdAt(cartItem.getCreatedAt())
            .updatedAt(cartItem.getUpdatedAt())
            .deleteAt(cartItem.getDeleteAt())
            .build();
    }

    public CartItem toModel() {
        return CartItem.builder()
            .id(this.id)
            .user(this.userEntity.toModel())
            .product(this.productEntity.toModel())
            .quantity(this.quantity)
            .createdAt(this.getCreatedAt())
            .updatedAt(this.getUpdatedAt())
            .deleteAt(this.getDeleteAt())
            .build();
    }
}
