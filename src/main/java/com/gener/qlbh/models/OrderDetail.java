package com.gener.qlbh.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "order_detail")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;
    private Double price;
    private Double length;
    private Double quantity;
    private String baseUnit;
    private Long inventoryId;

    @Column(name="line_index")
    private int lineIndex;

    public Double getSubtotal(){
        if (this.length==null||this.length==0) return this.price*this.quantity;
        return this.length*this.quantity*this.price;
    }

    public Double getTotalQuantity(){
        if (this.length==null||this.length==0) return quantity;
        return this.length*this.quantity;
    }

}
