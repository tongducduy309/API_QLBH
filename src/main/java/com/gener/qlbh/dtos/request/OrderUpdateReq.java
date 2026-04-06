package com.gener.qlbh.dtos.request;

import com.gener.qlbh.enums.OrderStatus;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class OrderUpdateReq {

    private Long customerId;
    private String nameCustomer;
    private String phoneCustomer;
    private String addressCustomer;
    private Double tax;
    private String note;
    private Double paidAmount;
    private Double shippingFee;
    private List<OrderDetailUpdateReq> orderDetailUpdateReqs;
    private String createdAt;
    private OrderStatus status;
}
