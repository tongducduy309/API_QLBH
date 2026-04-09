package com.gener.qlbh.controllers;

import com.gener.qlbh.dtos.request.PurchaseReceiptsCreateReq;
import com.gener.qlbh.exception.APIException;
import com.gener.qlbh.models.ResponseObject;
import com.gener.qlbh.services.PurchaseReceiptsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/purchase-receipts")
@RequiredArgsConstructor
public class PurchaseReceiptsController {
    private final PurchaseReceiptsService purchaseReceiptsService;

    @PostMapping
    ResponseEntity<ResponseObject> createPurchaseReceipts(@RequestBody PurchaseReceiptsCreateReq req) throws APIException{
        return purchaseReceiptsService.createPurchaseReceipts(req);
    }

    @GetMapping
    ResponseEntity<ResponseObject> getAllPurchaseReceipts(){
        return purchaseReceiptsService.getAllPurchaseReceipts();
    }

    @DeleteMapping("/{id}")
    ResponseEntity<ResponseObject> deletePurchaseReceipts(@PathVariable Long id) throws APIException {
        return purchaseReceiptsService.deletePurchaseReceipts(id);
    }

    @GetMapping("/product/{productId}")
    ResponseEntity<ResponseObject> getAllPurchaseReceiptsByProductId(@PathVariable Long productId){
        return purchaseReceiptsService.getAllPurchaseReceiptsByProductId(productId);
    }

    @GetMapping("/{id}")
    ResponseEntity<ResponseObject> getPurchaseReceiptDetail(@PathVariable Long id) throws APIException {
        return purchaseReceiptsService.getPurchaseReceiptDetail(id);
    }
}
