package com.gener.qlbh.dtos.response;

import com.gener.qlbh.enums.OrderDetailKind;
import com.gener.qlbh.models.Customer;
import jakarta.persistence.Column;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class OrderDetailRes {
    private Long id;
    private Double length;
    private Customer customer;
    private Double quantity;
    private Double price;
    private Double totalQuantity;//
    private Double subtotal;//
    private String sku;
    private String name;
    private Long productVariantId;
    private String baseUnit;
    private Long inventoryId;
    private OrderDetailKind kind;
    private String inventoryCode;
}
