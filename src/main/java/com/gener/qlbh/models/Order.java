package com.gener.qlbh.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gener.qlbh.context.TenantContext;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "orders")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Order {
    @Id
    @Column(name = "id", length = 13)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private String note;
    @Builder.Default
    private Double paidAmount= 0d;//Tiền nhận -- 200.000 | 50.000 (I-0)
    @Builder.Default
    private Double remainingAmount= 0d;//Tiền còn lại -- -90.000 = shippingFee+subtotal-paidAmount+changeAmount-paidDept = 110.000-50.000+0-0 = 50.000 (C-3)
    @Builder.Default
    private Double shippingFee= 0d;//Tiền vận chuyển -- 10.000 (I-0)
    @Builder.Default
    private Double subtotal= 0d;//Tiền hàng -- 100.000 (I-Order Details-0)
    @Builder.Default
    private Double paidDept = 0d;//Tiền trả sau -- 10.000 (I-2)
    @Builder.Default
    private Double changeAmount= 0d;//Tiền thối lại -- 90.000 | 0 = paidAmount - shippingFee+subtotal(paidAmount > shippingFee+subtotal) (C-1)

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<OrderDetail> details = new HashSet<>();

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() { if (createdAt==null) this.createdAt = LocalDateTime.now();
    _fillCompany();
    }


    public void addDetail(OrderDetail d) {
        details.add(d);
        d.setOrder(this);
    }
    public void removeDetail(OrderDetail d) {
        details.remove(d);
        d.setOrder(null);
    }

    public void setAmount(){
        changeAmount = (paidAmount > shippingFee+subtotal)?(paidAmount - (shippingFee+subtotal)):0;
        remainingAmount = (shippingFee+subtotal)-paidAmount+changeAmount-paidDept;
//        log.info(paidAmount+" "+subtotal+" "+shippingFee+" "+changeAmount+" "+remainingAmount);
    }

    public Double getTotal(){
        return shippingFee+subtotal;
    }


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    public void _fillCompany() {
        if (this.company == null) {
            this.company = TenantContext.get(); // lấy từ context nếu có
        }
        if (this.company == null) {
            throw new IllegalStateException("Product.company must not be null");
        }
    }

}

