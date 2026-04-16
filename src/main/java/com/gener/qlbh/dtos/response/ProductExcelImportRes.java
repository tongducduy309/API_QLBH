package com.gener.qlbh.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductExcelImportRes {

    private int totalRows;
    private int createdProducts;
    private int createdVariants;
    private int skippedRows;

    @Builder.Default
    private List<String> errors = new ArrayList<>();

    @Builder.Default
    private List<String> warnings = new ArrayList<>();

    public static ProductExcelImportRes empty() {
        return ProductExcelImportRes.builder()
                .totalRows(0)
                .createdProducts(0)
                .createdVariants(0)
                .skippedRows(0)
                .errors(new ArrayList<>())
                .warnings(new ArrayList<>())
                .build();
    }
}