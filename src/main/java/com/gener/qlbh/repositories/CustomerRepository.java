package com.gener.qlbh.repositories;

import com.gener.qlbh.dtos.response.CustomerDetailRes;
import com.gener.qlbh.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;


public interface CustomerRepository extends JpaRepository<Customer,Long> {
    @Query(value = """
    SELECT COALESCE(SUM(
        COALESCE(shipping_fee,0)
      + COALESCE(subtotal,0)
      + (COALESCE(tax,0) / 100.0) * COALESCE(subtotal,0)
    )/1000, 0)
    FROM orders
    WHERE customer_id = :cid
""", nativeQuery = true)
    Double getPoint(@Param("cid") Long cid);

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
