package com.gener.qlbh.controllers;

import com.gener.qlbh.dtos.request.OrderReq;
import com.gener.qlbh.dtos.request.ProductCreateReq;
import com.gener.qlbh.dtos.request.ProductUpdateReq;
import com.gener.qlbh.dtos.request.ProductWishlistUpdateReq;
import com.gener.qlbh.exception.APIException;
import com.gener.qlbh.models.ResponseObject;
import com.gener.qlbh.services.InventoryService;
import com.gener.qlbh.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @GetMapping
    ResponseEntity<ResponseObject> getAllInventory(@RequestParam boolean status){
        return inventoryService.getAllInventory(status);
    }

    @PostMapping("/available")
    ResponseEntity<ResponseObject> checkInventory(@RequestBody OrderReq orderReq) throws APIException {
        return inventoryService.checkInventory(orderReq);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<ResponseObject> deleteInventory(@PathVariable Long id){
        return inventoryService.deleteInventory(id);
    }

}
