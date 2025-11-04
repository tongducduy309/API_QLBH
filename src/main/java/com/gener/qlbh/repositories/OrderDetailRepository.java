package com.gener.qlbh.repositories;

import com.gener.qlbh.models.Category;
import com.gener.qlbh.models.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;


public interface OrderDetailRepository extends JpaRepository<OrderDetail,Long> {
}
