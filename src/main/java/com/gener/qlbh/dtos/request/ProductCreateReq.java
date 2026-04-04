package com.gener.qlbh.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProductCreateReq {
    @NotNull
    private String name;
    @NotNull
    private boolean active;
    private String baseUnit;
    @NotNull
    private String categoryName;
    private Double warningQuantity;
    private String description;

    private List<ProductVariantCreateReq> variants;
}
