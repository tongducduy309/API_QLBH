package com.gener.qlbh.dtos.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantCreateReq {
    private Long productId;
    private String variantCode;
    private String sku;
    private Double weight;
    private Double retailPrice;
    private Double storePrice;
    private Double costPrice;
    private Boolean status;
}
