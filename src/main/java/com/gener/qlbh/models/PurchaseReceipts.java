package com.gener.qlbh.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gener.qlbh.enums.PurchaseReceiptMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "purchase_receipts")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PurchaseReceipts {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @Enumerated(EnumType.STRING)
    private PurchaseReceiptMethod purchaseReceiptMethod;

    private Double totalQuantity;

    private Double cost;

    private Double totalCost;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private String supplier;

    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_lot_id", nullable = false)
    private InventoryLot inventory;


    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public String getName(){
        if (variant==null) return "Không xác định";
        return variant.getProduct().getName();
    }
}
