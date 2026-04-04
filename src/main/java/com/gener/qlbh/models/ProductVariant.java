package com.gener.qlbh.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_variants")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sku;

    private String variantCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonBackReference
    private Product product;

    private Double weight;

    private Double retailPrice;

    private Double storePrice;

    @Builder.Default
    private boolean active=true;

    @OneToMany(mappedBy = "variant", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE }, orphanRemoval = true)
    @ToString.Exclude
    @JsonManagedReference
    private List<InventoryLot> inventories = new ArrayList<>();

    public void addInventory(InventoryLot inv) {
        if (inv == null) return;
        inventories.add(inv);
        inv.setVariant(this);
    }

    public void removeInventory(InventoryLot inv) {
        if (inv == null) return;
        inventories.remove(inv);
        inv.setVariant(null);
    }


    public boolean getBusinessStatus(){
        return this.active && this.product.isActive();
    }
}
