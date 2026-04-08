package com.gener.qlbh.controllers;

import com.gener.qlbh.dtos.request.CustomerCreateReq;
import com.gener.qlbh.dtos.request.CustomerUpdateReq;
import com.gener.qlbh.exception.APIException;
import com.gener.qlbh.models.ResponseObject;
import com.gener.qlbh.services.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @GetMapping
    ResponseEntity<ResponseObject> getAllCustomers(){
        return customerService.getAllCustomers();
    }

    @PostMapping
    ResponseEntity<ResponseObject> createCustomer(@RequestBody CustomerCreateReq req){
        return customerService.createCustomer(req);
    }

    @PutMapping("/{id}")
    ResponseEntity<ResponseObject> updateCustomer(@PathVariable Long id, @RequestBody CustomerUpdateReq req) throws APIException {
        return customerService.updateCustomer(id,req);
    }

    @GetMapping("/{id}")
    ResponseEntity<ResponseObject> getCustomerById(@PathVariable Long id) throws APIException {
        return customerService.getCustomerById(id);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<ResponseObject> deleteCustomer(@PathVariable Long id){
        return customerService.deleteCustomer(id);
    }

    @GetMapping("/with-debt")
    public ResponseEntity<ResponseObject> getAllCustomersWithDebt() {
        return customerService.getAllCustomersWithDebt();
    }
}
