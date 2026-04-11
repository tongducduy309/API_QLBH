package com.gener.qlbh.controllers;

import com.gener.qlbh.dtos.request.InventoryExcelExportReq;
import com.gener.qlbh.dtos.request.OrderCreateReq;
import com.gener.qlbh.exception.APIException;
import com.gener.qlbh.models.ResponseObject;
import com.gener.qlbh.services.InventoryExcelService;
import com.gener.qlbh.services.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = "/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;
    private final InventoryExcelService inventoryExcelService;
    @GetMapping
    ResponseEntity<ResponseObject> getAllInventory(@RequestParam boolean status){
        return inventoryService.getAllInventory(status);
    }

    @PostMapping("/available")
    ResponseEntity<ResponseObject> checkInventory(@RequestBody OrderCreateReq orderCreateReq) throws APIException {
        return inventoryService.checkInventory(orderCreateReq);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<ResponseObject> deleteInventory(@PathVariable Long id){
        return inventoryService.deleteInventory(id);
    }

    @PostMapping("/excel/export")
    public ResponseEntity<byte[]> exportInventoryExcel(@RequestBody InventoryExcelExportReq req) throws Exception {
        byte[] file = inventoryExcelService.exportInventoryExcel(
                inventoryService.listInventoryRows(req.getOnlyActive() == null || req.getOnlyActive()),
                req
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=hang-hoa.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @PostMapping(value = "/excel/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseObject> importInventoryExcel(@RequestPart("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(200)
                        .message("Import inventory excel successfully")
                        .data(inventoryExcelService.importInventoryExcel(file))
                        .build()
        );
    }

}
