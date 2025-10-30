package com.gener.qlbh.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class OrderDetailReq {
    @NotNull
    private String name;
    private String productId;
    private Double length;
    private Double quantity;
    @NotNull
    private Double price;
    private String baseUnit;
}
