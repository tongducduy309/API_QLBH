package com.gener.qlbh.mapper;

import com.gener.qlbh.dtos.request.EmployeeCreateReq;
import com.gener.qlbh.dtos.request.EmployeeUpdateReq;
import com.gener.qlbh.dtos.response.EmployeeDetailRes;
import com.gener.qlbh.models.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    Employee toEmployee(EmployeeCreateReq req);

    Employee toEmployee(EmployeeUpdateReq req);

//    @Mapping(target = "userId", source = "user.id")
//    @Mapping(target = "username", source = "user.username")
//    @Mapping(target = "email", source = "user.email")
//    @Mapping(target = "roles", source = "user.roles")
    EmployeeDetailRes toEmployeeDetailRes(Employee employee);
}