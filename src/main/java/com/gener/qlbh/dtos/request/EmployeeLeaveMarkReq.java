package com.gener.qlbh.dtos.request;

import com.gener.qlbh.enums.LeaveType;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeLeaveMarkReq {
    private LocalDate leaveDate;
    private LeaveType leaveType;
    private String reason;
}