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
//    @GeneratedValue(strategy = GenerationType.UUID)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "product_id", nullable = false)
    @OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @JsonBackReference
    private Product product;

    @Column(nullable = false)
    private Double totalBaseUnitQty;


    public void exportInventory(Double totalQuantity){
        this.totalBaseUnitQty-=totalQuantity;
    }

    public void importInventory(Double totalQuantity){
        this.totalBaseUnitQty+=totalQuantity;
    }




}
