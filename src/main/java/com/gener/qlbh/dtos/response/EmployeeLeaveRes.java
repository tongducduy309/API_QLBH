package com.gener.qlbh.dtos.response;

import com.gener.qlbh.enums.LeaveType;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeLeaveRes {
    private Long id;
    private LocalDate leaveDate;
    private LeaveType leaveType;
    private String reason;
}