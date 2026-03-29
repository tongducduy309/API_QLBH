package com.gener.qlbh.repositories;

import com.gener.qlbh.models.Category;
import com.gener.qlbh.models.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface InventoryRepository extends JpaRepository<Inventory,Long> {
    Inventory findByVariantId(Long id);
    Optional<Inventory> findActiveByVariantId(Long id);

    @Query("""
        SELECT i
        FROM Inventory i
        JOIN FETCH i.variant v
        JOIN FETCH v.product p
        WHERE v.status = true
          AND p.status = true
    """)
    List<Inventory> findAllActiveVariantAndProduct(@Param("status") boolean status);
}
