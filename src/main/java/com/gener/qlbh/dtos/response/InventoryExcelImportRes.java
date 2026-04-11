package com.gener.qlbh.dtos.response;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryExcelImportRes {
    private int totalRows;
    private int createdProducts;
    private int updatedProducts;
    private int createdVariants;
    private int updatedVariants;
    private int createdLots;
    private int updatedLots;
    private List<String> warnings;
    private List<String> errors;

    public static InventoryExcelImportRes empty() {
        return InventoryExcelImportRes.builder()
                .totalRows(0)
                .createdProducts(0)
                .updatedProducts(0)
                .createdVariants(0)
                .updatedVariants(0)
                .createdLots(0)
                .updatedLots(0)
                .warnings(new ArrayList<>())
                .errors(new ArrayList<>())
                .build();
    }
}