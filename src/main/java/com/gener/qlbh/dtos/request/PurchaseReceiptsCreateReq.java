package com.gener.qlbh.dtos.request;

import com.gener.qlbh.enums.PurchaseReceiptMethod;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PurchaseReceiptsCreateReq {
    private String lotCode;
    private Long productVariantId;
    private PurchaseReceiptMethod purchaseReceiptMethod;
    private Double totalQuantity;
    private Double cost;
    private String supplier;
    private String note;
    private Double totalCost;
}
