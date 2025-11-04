package com.gener.qlbh.repositories;

import com.gener.qlbh.models.Category;
import com.gener.qlbh.models.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;


public interface InventoryRepository extends JpaRepository<Inventory,Long> {
    Inventory findByProductId(Long id);
}
