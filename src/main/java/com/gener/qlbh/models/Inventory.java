package com.gener.qlbh.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_inventory_code", columnNames = "inventory_code")
        })
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false)
    @JsonBackReference
    private ProductVariant variant;

    @Column(name = "inventory_code", nullable = false, unique = true, length = 20)
    private String inventoryCode;

    @Column(nullable = false)
    private Double originalQty;

    @Column(nullable = false)
    private Double remainingQty;

    @Column(nullable = false)
    private Double costPrice;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    private LocalDateTime importedAt;

    public boolean isOutOfStock() {
        return remainingQty == null || remainingQty <= 0;
    }

    public void deduct(Double qty) {
        if (qty == null || qty <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0");
        }
        if (remainingQty < qty) {
            throw new IllegalStateException("Insufficient stock in lot");
        }
        remainingQty -= qty;
        if (remainingQty == 0) {
            active = false;
        }
    }

    public void restore(Double qty) {
        if (qty == null || qty <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0");
        }
        remainingQty += qty;
        if (remainingQty > 0) {
            active = true;
        }
    }


//    public void exportInventory(Double totalQuantity){
//        this.totalQty-=totalQuantity;
//    }
//
//    public void addQuantity(Double totalQuantity){
//        this.totalQty+=totalQuantity;
//    }
//
//    public void subQuantity(Double totalQuantity){
//        this.totalQty=this.totalQty>totalQuantity?this.totalQty-totalQuantity:0;
//
//    }




}
