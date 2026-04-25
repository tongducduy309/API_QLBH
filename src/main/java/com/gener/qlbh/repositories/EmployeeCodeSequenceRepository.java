package com.gener.qlbh.repositories;

import com.gener.qlbh.models.EmployeeCodeSequence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeCodeSequenceRepository extends JpaRepository<EmployeeCodeSequence, Long> {
}