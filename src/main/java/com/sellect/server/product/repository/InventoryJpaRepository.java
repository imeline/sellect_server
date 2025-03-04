package com.sellect.server.product.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryJpaRepository extends JpaRepository<InventoryEntity, Long> {

    Optional<InventoryEntity> findByProductEntityId(Long productId);
}
