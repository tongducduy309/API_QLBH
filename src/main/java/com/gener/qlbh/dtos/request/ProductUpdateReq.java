package com.gener.qlbh.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductUpdateReq {
    private String sku;
    @NotNull
    private String name;
    private Double retailPrice;
    private Double storePrice;
    private boolean status;
    private String baseUnit;
    @NotNull
    private Long categoryId;
}
