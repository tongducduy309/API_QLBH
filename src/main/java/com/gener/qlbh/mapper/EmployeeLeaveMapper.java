package com.gener.qlbh.mapper;

import com.gener.qlbh.dtos.response.EmployeeLeaveRes;
import com.gener.qlbh.models.EmployeeLeave;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmployeeLeaveMapper {
    EmployeeLeaveRes toRes(EmployeeLeave leave);
}