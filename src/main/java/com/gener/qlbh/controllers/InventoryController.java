package com.gener.qlbh.controllers;

import com.gener.qlbh.dtos.request.OrderCreateReq;
import com.gener.qlbh.exception.APIException;
import com.gener.qlbh.models.ResponseObject;
import com.gener.qlbh.services.InventoryService;
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
    ResponseEntity<ResponseObject> checkInventory(@RequestBody OrderCreateReq orderCreateReq) throws APIException {
        return inventoryService.checkInventory(orderCreateReq);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<ResponseObject> deleteInventory(@PathVariable Long id){
        return inventoryService.deleteInventory(id);
    }

}
