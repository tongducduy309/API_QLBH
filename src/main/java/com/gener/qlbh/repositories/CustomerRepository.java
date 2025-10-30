package com.gener.qlbh.repositories;

import com.gener.qlbh.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CustomerRepository extends JpaRepository<Customer,Long> {
}
