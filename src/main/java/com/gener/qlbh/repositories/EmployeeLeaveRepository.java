package com.gener.qlbh.repositories;


import com.gener.qlbh.models.EmployeeLeave;
import com.gener.qlbh.models.EmployeePayroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmployeeLeaveRepository extends JpaRepository<EmployeeLeave, Long> {
    List<EmployeeLeave> findByEmployeeIdAndLeaveDateBetween(
            Long employeeId, LocalDate from, LocalDate to
    );

    @Query("""
        select coalesce(sum(case
            when l.leaveType = com.gener.qlbh.enums.LeaveType.FULL_DAY then 1.0
            else 0.5 end), 0)
        from EmployeeLeave l
        where l.employee.id = :employeeId
          and month(l.leaveDate) = :month
          and year(l.leaveDate) = :year
    """)
    Double sumLeaveDays(@Param("employeeId") Long employeeId,
                        @Param("month") int month,
                        @Param("year") int year);
}

