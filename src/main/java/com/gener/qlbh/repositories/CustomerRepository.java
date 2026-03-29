package com.gener.qlbh.repositories;

import com.gener.qlbh.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;


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
}
