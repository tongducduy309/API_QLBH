package com.gener.qlbh.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

    private String nameCustomer;
    private String phoneCustomer;
    private String addressCustomer;

    @Builder.Default
    private Double tax=0d;

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
    private int modify = 0;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @OrderBy("line_index ASC")
    private Set<OrderDetail> details = new HashSet<>();

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() { if (createdAt==null) this.createdAt = LocalDateTime.now(); }


    public void addDetail(OrderDetail d) {
        details.add(d);
        d.setOrder(this);
    }
    public void removeDetail(OrderDetail d) {
        details.remove(d);
        d.setOrder(null);
    }

    public Double getTaxAmount(){
        if (this.tax==null) return 0d;
        return (this.tax/100)*this.subtotal;
    }

    public void setAmount(){
        Double total = getTotal();
        changeAmount = (paidAmount > total)?(paidAmount - total):0;
        remainingAmount = total-paidAmount+changeAmount-paidDept;
    }

    public Double getTotal(){
        return shippingFee+subtotal+getTaxAmount();
    }

    public Long getIdCustomer(){
        if (this.customer!=null) return this.customer.getId();
        return null;
    }

    public String getNameCustomer(){
        if (this.customer!=null) return this.customer.getName();
        return this.nameCustomer;
    }

    public String getPhoneCustomer(){
        if (this.customer!=null) return this.customer.getPhone();
        return this.phoneCustomer;
    }

    public String getAddressCustomer(){
        if (this.customer!=null) return this.customer.getAddress();
        return this.addressCustomer;
    }




}

