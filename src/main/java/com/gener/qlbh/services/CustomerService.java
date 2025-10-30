package com.gener.qlbh.services;

import com.gener.qlbh.dtos.request.CustomerCreateReq;
import com.gener.qlbh.dtos.request.CustomerUpdateReq;
import com.gener.qlbh.dtos.request.ProductUpdateReq;
import com.gener.qlbh.enums.ErrorCode;
import com.gener.qlbh.enums.SuccessCode;
import com.gener.qlbh.exception.APIException;
import com.gener.qlbh.mapper.CustomerMapper;
import com.gener.qlbh.models.Category;
import com.gener.qlbh.models.Customer;
import com.gener.qlbh.models.Product;
import com.gener.qlbh.models.ResponseObject;
import com.gener.qlbh.repositories.CategoryRepository;
import com.gener.qlbh.repositories.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public ResponseEntity<ResponseObject> getAllCustomers(){
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Get All Customer Successfully")
                        .data(customerRepository.findAll())
                        .build()
        );

    }

    @Transactional
    public ResponseEntity<ResponseObject> getCustomerById(Long id) throws APIException {
        Customer customer = customerRepository.findById(id).orElseThrow(()-> APIException.builder()
                .status(ErrorCode.NOT_FOUND.getStatus())
                .message("Cannot Found Customer With Id = "+id)
                .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                .build());
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Get Customer With Id = "+id+" Successfully")
                        .data(customer)
                        .build()
        );

    }

    @Transactional
    public ResponseEntity<ResponseObject> createCustomer(CustomerCreateReq req){
        Customer customer = customerMapper.toCustomer(req);
        return ResponseEntity.status(SuccessCode.CREATE.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.CREATE.getStatus())
                        .message("Create Product Successfully")
                        .data(customerRepository.save(customer))
                        .build()
        );

    }

    @Transactional
    public ResponseEntity<ResponseObject> deleteCustomer(Long id){
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isPresent()){
            customerRepository.deleteById(id);

        }
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                new ResponseObject(SuccessCode.REQUEST.getStatus(), "Delete Customer Successfully","")
        );
    }

    @Transactional
    public ResponseEntity<ResponseObject> updateCustomer(Long id, CustomerUpdateReq req) throws APIException {
        boolean existsCustomer = customerRepository.existsById(id);
        if (!existsCustomer) {
            throw APIException.builder()
                    .status(ErrorCode.NOT_FOUND.getStatus())
                    .message("Cannot Found Product With Id = " + id)
                    .httpStatusCode(ErrorCode.NOT_FOUND.getHttpStatusCode())
                    .build();
        }

        Customer newCustomer = customerMapper.toCustomer(req);
        newCustomer.setId(id);

        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                new ResponseObject(SuccessCode.REQUEST.getStatus(), "Update Customer Successfully",customerRepository.save(newCustomer))
        );
    }
}
