package com.gener.qlbh.dtos.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gener.qlbh.enums.PurchaseReceiptMethod;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PurchaseReceiptDetailRes {
    private Long id;

    private Long productId;
    private String productName;

    private Long productVariantId;
    private String productVariantCode;
    private String productVariantSKU;
    private String productVariantWeight;

    private PurchaseReceiptMethod purchaseReceiptMethod;

    private Double totalQuantity;
    private Double cost;
    private Double totalCost;
    private String supplier;
    private String note;

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdAt;

    private Long inventoryId;
    private String inventoryCode;
    private Double inventoryOriginalQty;
    private Double inventoryRemainingQty;
    private Double inventoryCostPrice;
    private Boolean inventoryActive;

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime inventoryImportedAt;


}