package com.gener.qlbh.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductUpdateReq {
    @NotNull
    private String name;
    private boolean status;
    private String baseUnit;
    @NotNull
    private Long categoryId;
    private Double warningQuantity;
    private List<ProductVariantUpdateReq> variants;
}
