package com.gener.qlbh.dtos.request;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PurchaseOrderCreateReq {
    private String productId;
    private Double stockingQty;
    private Double totalLength;
    private Double costPerUnit;
    private String supplier;
}
