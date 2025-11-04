package com.gener.qlbh.dtos.request;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PurchaseOrderCreateReq {
    private Long productId;
    private Double stockingQty;
    private Double totalLength;
    private Double costPerUnit;
    private String supplier;
    private String note;
}
