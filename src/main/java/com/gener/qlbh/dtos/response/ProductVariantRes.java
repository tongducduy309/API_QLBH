package com.gener.qlbh.dtos.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantRes {
    private Long id;
    private String variantCode;
    private String weight;
    private Double retailPrice;
    private Double storePrice;
    private boolean active;
    private String productName;
}
