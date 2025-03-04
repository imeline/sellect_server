package com.sellect.server.product.repository;

import com.sellect.server.product.domain.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InventoryRepositoryImpl implements InventoryRepository {

    private final InventoryJpaRepository inventoryJpaRepository;

    @Override
    public Inventory save(Inventory inventory) {
        InventoryEntity inventoryEntity = inventoryJpaRepository.save(
            InventoryEntity.from(inventory));
        return inventoryEntity.toModel();
    }
}
