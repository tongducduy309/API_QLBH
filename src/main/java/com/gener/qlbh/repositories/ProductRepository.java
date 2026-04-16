package com.gener.qlbh.repositories;

import com.gener.qlbh.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


public interface ProductRepository extends JpaRepository<Product,Long> {
    @Query("""
    select distinct p
    from Product p
    left join p.variants v
    left join v.inventories i
""")
    List<Product> findAllWithVariantsAndInventories();


    Optional<Product> findFirstByVariants_Sku(String sku);

    Optional<Product> findByNameIgnoreCase(String name);

}
