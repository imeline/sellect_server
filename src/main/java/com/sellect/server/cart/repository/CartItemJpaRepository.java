package com.sellect.server.cart.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CartItemJpaRepository extends JpaRepository<CartItemEntity, Long> {

    @Query("SELECT c FROM CartItemEntity c "
        + "WHERE c.userEntity.id = :userId "
        + "AND c.deleteAt IS NULL")
    List<CartItemEntity> findByUserId(Long userId);

    Optional<CartItemEntity> findByUserEntityIdAndProductEntityIdAndDeleteAtIsNull(Long userId, Long productId);

    Long countByUserEntityIdAndDeleteAtIsNull(Long userEntityId);
}
