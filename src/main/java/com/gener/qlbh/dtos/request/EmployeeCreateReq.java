package com.gener.qlbh.dtos.request;

import com.gener.qlbh.enums.Role;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeCreateReq {
    private String fullName;
    private String phone;
    private String address;
    private String position;
    private LocalDate hireDate;
    private Double baseSalary;

    private String username;
    private String email;
    private Role role;
}