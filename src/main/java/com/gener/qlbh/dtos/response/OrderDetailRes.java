package com.gener.qlbh.dtos.response;

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
    private Double quantity;
    private Double price;
    private Double totalLength;//
    private Double subtotal;//
    private String sku;
    private String name;
    private Long productId;
    private String baseUnit;
}
