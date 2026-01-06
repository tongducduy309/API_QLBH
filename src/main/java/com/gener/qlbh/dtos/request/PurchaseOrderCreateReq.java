package com.gener.qlbh.dtos.request;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PurchaseOrderCreateReq {
    private Long productVariantId;
    private Double totalQty;
    private Double costPerUnit;
    private String supplier;
    private String note;
}
