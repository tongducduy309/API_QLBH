package com.gener.qlbh.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;

import java.math.BigDecimal;

@Entity
@Table(name = "inventory")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "product_id", nullable = false)
//
//    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    @JsonBackReference
    private ProductVariant variant;

    @Column(nullable = false)
    private Double totalQty;


//    public void exportInventory(Double totalQuantity){
//        this.totalQty-=totalQuantity;
//    }
//
    public void addQuantity(Double totalQuantity){
        this.totalQty+=totalQuantity;
    }




}
