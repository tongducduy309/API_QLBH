package com.gener.qlbh.repositories;

import com.gener.qlbh.models.PurchaseReceipts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PurchaseReceiptsRepository extends JpaRepository<PurchaseReceipts,Long> {
//    Optional<PurchaseReceipts> findTopByVariant_IdOrderByCreatedAtDesc(Long variantId);
    Optional<PurchaseReceipts> findTopByVariant_IdOrderByCreatedAtDesc(Long variantId);
}
