package com.gener.qlbh.dtos.response;

import com.gener.qlbh.enums.Role;
import com.gener.qlbh.models.User;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDetailRes {
    private Long id;
    private String code;
    private String fullName;
    private String phone;
    private String address;
    private String position;
    private LocalDate hireDate;
    private Double baseSalary;
    private Boolean active;
    private LocalDateTime createdAt;

    private User user;
}