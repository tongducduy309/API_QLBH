package com.gener.qlbh.dtos.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryExcelExportReq {
    private List<String> columns;
    private Boolean onlyActive;
}
