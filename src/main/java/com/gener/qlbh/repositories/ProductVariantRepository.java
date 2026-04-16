package com.gener.qlbh.repositories;

import com.gener.qlbh.models.Product;
import com.gener.qlbh.models.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface ProductVariantRepository extends JpaRepository<ProductVariant,Long> {
    Optional<ProductVariant> findFirstBySku(String sku);

    Optional<ProductVariant> findFirstBySkuIgnoreCase(String sku);

    Optional<ProductVariant> findFirstByProduct_IdAndVariantCodeIgnoreCase(Long productId, String variantCode);

    boolean existsBySkuIgnoreCase(String sku);
}
