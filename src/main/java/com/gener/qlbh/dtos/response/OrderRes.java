package com.gener.qlbh.dtos.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gener.qlbh.models.Customer;
import com.gener.qlbh.models.OrderDetail;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class OrderRes {
    private String id;
    private Customer customer;

    private String note;

    private Double paidAmount;
    private Double remainingAmount;
    private Double shippingFee;
    private Double subtotal;
    private Double paidDept ;
    private Double changeAmount;
    private Set<OrderDetailRes> details;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
