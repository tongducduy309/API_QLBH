package com.gener.qlbh.mapper;



import com.gener.qlbh.dtos.request.CustomerCreateReq;
import com.gener.qlbh.dtos.request.CustomerUpdateReq;
import com.gener.qlbh.models.Customer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    Customer toCustomer(CustomerCreateReq req);
    Customer toCustomer(CustomerUpdateReq req);
}
