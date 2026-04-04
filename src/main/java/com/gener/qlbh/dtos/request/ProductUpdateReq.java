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
    private boolean active;
    private String baseUnit;
    @NotNull
    private String categoryName;
    private Double warningQuantity;
    private String description;

    private List<ProductVariantUpdateReq> variants;
}
