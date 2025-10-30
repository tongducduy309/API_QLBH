package com.gener.qlbh.repositories;

import com.gener.qlbh.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface ProductRepository extends JpaRepository<Product,String> {
}
