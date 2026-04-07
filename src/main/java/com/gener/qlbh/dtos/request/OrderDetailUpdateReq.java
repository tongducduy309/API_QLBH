package com.gener.qlbh.dtos.request;

import com.gener.qlbh.enums.OrderDetailKind;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class OrderDetailUpdateReq {
    private Long id;
    @NotNull
    private String name;
    private Long productVariantId;
    private Long productId;
    private Double length;
    private Double quantity;
    @NotNull
    private Double price;
    private String baseUnit;
    private Long inventoryId;
    private int lineIndex;
    private OrderDetailKind kind;
}
