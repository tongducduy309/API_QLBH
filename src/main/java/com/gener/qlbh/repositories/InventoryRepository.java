package com.gener.qlbh.repositories;

import com.gener.qlbh.models.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface InventoryRepository extends JpaRepository<Inventory,Long> {
    Inventory findByVariantId(Long id);

    Optional<Inventory> findFirstByVariant_IdAndInventoryCode(Long productVariantId, String inventoryCode);

    boolean existsByInventoryCode(String inventoryCode);

    @Query("""
        select max(i.inventoryCode)
        from Inventory i
        where i.inventoryCode like concat(:prefix, '%')
    """)
    Optional<String> findMaxInventoryCodeByPrefix(@Param("prefix") String prefix);
}
