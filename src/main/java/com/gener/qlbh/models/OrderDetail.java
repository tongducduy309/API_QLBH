package com.gener.qlbh.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gener.qlbh.enums.Method;
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
    @JoinColumn(name = "product_id")
    private Product product;
    private Double price;
    private Double length;
    private Double quantity;
    private String baseUnit;

    public Double getSubtotal(){
        if (this.getProduct()==null) return this.price;
        if (this.getProduct().getCategory().getMethod()==Method.SHEET_METAL){
            return this.length*this.quantity*this.price;
        }
        return this.quantity*this.price;
    }

    public Double getTotalLength(){
        if (this.getProduct()==null||this.getProduct().getCategory().getMethod()==Method.MISC) return null;
        return this.length*this.quantity;
    }

    public Double getTotalQuantity(){
        if (this.getProduct()==null||this.getProduct().getCategory().getMethod()==Method.MISC) return quantity;
        return this.length*this.quantity;
    }

}
