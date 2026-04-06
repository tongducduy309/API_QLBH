package com.gener.qlbh.dtos.response;

import com.gener.qlbh.enums.PurchaseReceiptMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class PurchaseReceiptsRes {
    private Long id;
    private String productVariantCode;
    private String productVariantSKU;
    private PurchaseReceiptMethod purchaseReceiptMethod;
    private String name;
    private Double totalQuantity;
    private Double cost;
    private String supplier;
    private String note;
    private LocalDateTime createdAt;
}
