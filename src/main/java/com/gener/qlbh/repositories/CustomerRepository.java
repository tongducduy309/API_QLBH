package com.gener.qlbh.repositories;

import com.gener.qlbh.dtos.response.CustomerDetailRes;
import com.gener.qlbh.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;


public interface CustomerRepository extends JpaRepository<Customer,Long> {


    @Query("""
        SELECT new com.gener.qlbh.dtos.response.CustomerDetailRes(
            c.id,
            c.name,
            c.phone,
            c.email,
            c.taxCode,
            c.address,
            c.createdAt,
            COALESCE(SUM(o.remainingAmount), 0)
        )
        FROM Customer c
        LEFT JOIN Order o ON o.customer.id = c.id AND o.remainingAmount > 0
        GROUP BY c.id, c.name, c.phone, c.address
        ORDER BY c.id DESC
    """)
    List<CustomerDetailRes> findAllCustomersWithDebt();


}
