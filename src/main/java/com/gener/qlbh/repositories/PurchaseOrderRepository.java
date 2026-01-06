package com.gener.qlbh.repositories;

import com.gener.qlbh.models.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder,Long> {
//    Optional<PurchaseOrder> findTopByProductIdOrderByCreatedAtDesc(Long productId);
}
