package com.gener.qlbh.repositories;

import com.gener.qlbh.models.Product;
import com.gener.qlbh.models.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ProductVariantRepository extends JpaRepository<ProductVariant,Long> {
}
