package com.gener.qlbh.dtos.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantInventoryRes {
    private Long inventoryId;
    private String sku;
    private String lotCode;
    private Double originalQty;
    private boolean outOfStock;
    private Long variantId;
    private String variantCode;
    private String weight;
    private Double retailPrice;
    private Double storePrice;
    private Double remainingQty;
    private Double costPrice;
    private Boolean active;
}
