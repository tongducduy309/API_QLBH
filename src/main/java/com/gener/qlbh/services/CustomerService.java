package com.gener.qlbh.services;

import com.gener.qlbh.dtos.request.CustomerCreateReq;
import com.gener.qlbh.dtos.request.CustomerUpdateReq;
import com.gener.qlbh.dtos.request.ProductUpdateReq;
import com.gener.qlbh.dtos.response.CustomerDetailRes;
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
import com.gener.qlbh.repositories.OrderRepository;
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
    private final OrderRepository orderRepository;

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
        Customer customer = customerRepository.findById(id).orElseThrow(
                ()->new APIException(ErrorCode.USER_NOT_FOUND)
        );
        Double debt = orderRepository.getDebtByCustomerId(id);
        CustomerDetailRes customerDetailRes = customerMapper.toCustomerDetailRes(customer);
        customerDetailRes.setTotalDebt(debt);
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Get Customer With Id = "+id+" Successfully")
                        .data(customerDetailRes)
                        .build()
        );

    }

    @Transactional(rollbackOn  = Exception.class)
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

    @Transactional(rollbackOn  = Exception.class)
    public ResponseEntity<ResponseObject> deleteCustomer(Long id){
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isPresent()){
            customerRepository.deleteById(id);

        }
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                new ResponseObject(SuccessCode.REQUEST.getStatus(), "Delete Customer Successfully","")
        );
    }

    @Transactional(rollbackOn  = Exception.class)
    public ResponseEntity<ResponseObject> updateCustomer(Long id, CustomerUpdateReq req) throws APIException {
        Customer customer = customerRepository.findById(id).orElseThrow(
                ()->new APIException(ErrorCode.USER_NOT_FOUND)
        );

        customer.setAddress(req.getAddress());
        customer.setName(req.getName());
        customer.setPhone(req.getPhone());
        customer.setTaxCode(req.getTaxCode());
        customer.setEmail(req.getEmail());


        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                new ResponseObject(SuccessCode.REQUEST.getStatus(), "Update Customer Successfully",customerRepository.save(customer))
        );
    }

    public ResponseEntity<ResponseObject> getAllCustomersWithDebt() {
        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Get all customers with debt successfully")
                        .data(customerRepository.findAllCustomersWithDebt())
                        .build()
        );
    }
}
