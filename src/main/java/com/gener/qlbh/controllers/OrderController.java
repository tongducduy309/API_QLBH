package com.gener.qlbh.controllers;

import com.gener.qlbh.dtos.request.*;
import com.gener.qlbh.exception.APIException;
import com.gener.qlbh.models.ResponseObject;
import com.gener.qlbh.services.OrderService;
import com.gener.qlbh.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    ResponseEntity<ResponseObject> getAllOrders(){
        return orderService.getAllOrders();
    }

    @PostMapping
    ResponseEntity<ResponseObject> createOrder(@RequestBody OrderReq req) throws APIException {
        return orderService.createOrder(req);
    }

    @PutMapping("/{id}")
    ResponseEntity<ResponseObject> updateOrder(@PathVariable String id, @RequestBody OrderUpdateReq req) throws APIException {
        return orderService.updateOrder(id, req);
    }

    @GetMapping("/{id}")
    ResponseEntity<ResponseObject> getOrderById(@PathVariable String id) throws APIException {
        return orderService.getOrderById(id);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<ResponseObject> deleteOrder(@PathVariable String id){
        return orderService.deleteOrder(id);
    }

    @PostMapping("/amount")
    ResponseEntity<ResponseObject> paidDeptOrder(@RequestBody PaidDeptReq req) throws APIException{
        return orderService.paidDeptOrder(req);
    }

    @GetMapping("/customer/{customerId}")
    ResponseEntity<ResponseObject> getDeptOrderByCustomerId(@PathVariable Long customerId) throws APIException{
        return orderService.getDeptOrderByCustomerId(customerId);
    }
}
