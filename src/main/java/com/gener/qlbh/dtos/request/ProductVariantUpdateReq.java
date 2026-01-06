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
    private Double weight;
    private String sku;
    private Double retailPrice;
    private Double storePrice;
    private Double costPrice;
    private Boolean status;
}
