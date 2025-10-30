package com.gener.qlbh.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.gener.qlbh.context.TenantContext;
import com.gener.qlbh.enums.Method;
import com.gener.qlbh.utils.StringUtil;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "products")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String sku;

    @Column(nullable = false,length = 100,unique = true)
    private String name;



    private Double retailPrice;

    private Double storePrice;

    @Builder.Default
    private boolean status=true;

    @Builder.Default
    private boolean wishlist=false;

    private String baseUnit;

    private Double costPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
//    @JsonBackReference
    private Category category;

    @OneToOne(mappedBy = "product", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE }, orphanRemoval = true)
    @ToString.Exclude
//    @JsonManagedReference
    private Inventory inventory;

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
        if (inventory != null) {
            inventory.setProduct(this);
        }
    }

    @PrePersist
    public void _fillCompany() {
        if (this.company == null) {
            this.company = TenantContext.get(); // lấy từ context nếu có
        }
        if (this.company == null) {
            throw new IllegalStateException("Product.company must not be null");
        }
    }







}
