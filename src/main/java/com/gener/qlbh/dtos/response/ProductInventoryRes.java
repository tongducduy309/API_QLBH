package com.gener.qlbh.dtos.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductInventoryRes {
    private Long id;
    private String sku;
    private String name;
    private String categoryName;
    private String baseUnit;
    private String description;
    private Boolean active;
    private Double stock;
    private String status;
    private List<ProductVariantInventoryRes> variants;
}
