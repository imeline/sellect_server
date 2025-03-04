package com.sellect.server.product.repository;

import com.sellect.server.product.domain.Inventory;
import java.util.Optional;

public interface InventoryRepository {

    Inventory save(Inventory inventory);

    Optional<Inventory> findByProductId(Long productId);

    Optional<Inventory> findById(Long id);
}
