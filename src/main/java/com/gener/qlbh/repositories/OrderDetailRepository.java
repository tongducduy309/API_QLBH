package com.gener.qlbh.repositories;

import com.gener.qlbh.models.Category;
import com.gener.qlbh.models.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;


public interface OrderDetailRepository extends JpaRepository<OrderDetail,Long> {
    List<OrderDetail> findByProductVariant_IdIn(Collection<Long> variantIds);
    boolean existsByProductVariant_IdIn(Collection<Long> variantIds);
}
