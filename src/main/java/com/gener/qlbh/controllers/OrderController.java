package com.gener.qlbh.controllers;

import com.gener.qlbh.dtos.request.*;
import com.gener.qlbh.exception.APIException;
import com.gener.qlbh.models.ResponseObject;
import com.gener.qlbh.services.OrderService;
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
    ResponseEntity<ResponseObject> createOrder(@RequestBody OrderCreateReq req) throws APIException {
        return orderService.createOrder(req);
    }

    @PutMapping("/{id}")
    ResponseEntity<ResponseObject> updateOrder(@PathVariable Long id, @RequestBody OrderUpdateReq req) throws APIException {
        return orderService.updateOrder(id, req);
    }

    @GetMapping("/{id}")
    ResponseEntity<ResponseObject> getOrderById(@PathVariable Long id) throws APIException {
        return orderService.getOrderById(id);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<ResponseObject> deleteOrder(@PathVariable Long id) throws APIException {
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

    @GetMapping("/next-code")
    ResponseEntity<ResponseObject> getNextOrderCode(){
        return orderService.getNextOrderCode();
    }

    @GetMapping("/recent")
    ResponseEntity<ResponseObject> getNextOrderCode(@RequestParam Long amount){
        return orderService.getOrdersRecent(amount);
    }
}
