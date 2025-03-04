package com.sellect.server.product.repository;

import com.sellect.server.product.domain.Inventory;

public interface InventoryRepository {

    Inventory save(Inventory inventory);
}
