package com.gener.qlbh.repositories;

import com.gener.qlbh.models.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByCode(String code);
    Optional<Employee> findByCode(String code);
    Optional<Employee> findByUserId(Long id);


}


