package com.gener.qlbh.repositories;

import com.gener.qlbh.models.PurchaseReceipts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PurchaseReceiptsRepository extends JpaRepository<PurchaseReceipts,Long> {
//    Optional<PurchaseReceipts> findTopByVariant_IdOrderByCreatedAtDesc(Long variantId);
    Optional<PurchaseReceipts> findTopByVariant_IdOrderByCreatedAtDesc(Long variantId);

    @Query("""
            SELECT p FROM PurchaseReceipts p
            where p.variant.product.id = :productId
            """)
    List<PurchaseReceipts> findAllByProductId(@Param("productId") Long productId);
}
