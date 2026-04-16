package com.gener.qlbh.dtos.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryUpdateReq {
    private String inventoryCode;
    private Double remainingQty;
    private Double costPrice;

}
