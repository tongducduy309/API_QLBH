package com.gener.qlbh.repositories;

import com.gener.qlbh.models.InventoryLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface InventoryRepository extends JpaRepository<InventoryLot,Long> {
    InventoryLot findByVariantId(Long id);
    Optional<InventoryLot> findActiveByVariantId(Long id);

    @Query("""
        SELECT i
        FROM InventoryLot i
        JOIN FETCH i.variant v
        JOIN FETCH v.product p
        WHERE v.active = true
          AND p.active = true AND i.active = true
    """)
    List<InventoryLot> findAllActiveVariantAndProduct(@Param("status") boolean status);

    @Query("""
    select l
    from InventoryLot l
    where l.variant.id = :variantId
      and l.remainingQty > 0
      and l.active = true
    order by l.remainingQty asc
""")
    List<InventoryLot> findAvailableLots(@Param("variantId") Long variantId);

    Optional<InventoryLot> findFirstByVariant_IdAndLotCode(Long productVariantId, String lotCode);
}
