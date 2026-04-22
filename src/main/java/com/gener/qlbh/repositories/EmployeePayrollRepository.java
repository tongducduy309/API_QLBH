package com.gener.qlbh.repositories;

import com.gener.qlbh.models.EmployeePayroll;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeePayrollRepository extends JpaRepository<EmployeePayroll, Long> {
    Optional<EmployeePayroll> findByEmployeeIdAndMonthAndYear(Long employeeId, Integer month, Integer year);
    List<EmployeePayroll> findByMonthAndYear(Integer month, Integer year);
}
