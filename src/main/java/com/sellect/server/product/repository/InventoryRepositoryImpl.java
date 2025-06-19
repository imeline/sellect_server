package com.sellect.server.product.repository;

import com.sellect.server.product.domain.Inventory;
import java.util.Optional;
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

    @Override
    public Optional<Inventory> findByProductId(Long productId) {
        return inventoryJpaRepository.findByProductEntityId(productId)
            .map(InventoryEntity::toModel);
    }

    @Override
    public Optional<Inventory> findById(Long id) {
        return inventoryJpaRepository.findById(id)
            .map(InventoryEntity::toModel);
    }

    @Override
    public Optional<Inventory> findWithLockByProductId(Long productId) {
        return inventoryJpaRepository.findWithLockByProductEntityId(productId)
            .map(InventoryEntity::toModel);
    }
}
