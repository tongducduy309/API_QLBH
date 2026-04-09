package com.gener.qlbh.dtos.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantUpdateReq {
    private Long id;
    private String variantCode;
    private String sku;
    private String weight;
    private Double retailPrice;
    private Double storePrice;
    private Boolean active;
}
