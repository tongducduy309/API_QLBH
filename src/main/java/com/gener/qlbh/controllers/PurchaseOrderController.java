package com.gener.qlbh.controllers;

import com.gener.qlbh.dtos.request.PurchaseOrderCreateReq;
import com.gener.qlbh.exception.APIException;
import com.gener.qlbh.models.ResponseObject;
import com.gener.qlbh.services.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/purchase-order")
@RequiredArgsConstructor
public class PurchaseOrderController {
    private final PurchaseOrderService purchaseOrderService;

    @PostMapping
    ResponseEntity<ResponseObject> createPurchaseOrder(@RequestBody PurchaseOrderCreateReq req) throws APIException{
        return purchaseOrderService.createPurchaseOrder(req);
    }

    @GetMapping
    ResponseEntity<ResponseObject> getAllPurchaseOrder(){
        return purchaseOrderService.getAllPurchaseOrder();
    }
}
