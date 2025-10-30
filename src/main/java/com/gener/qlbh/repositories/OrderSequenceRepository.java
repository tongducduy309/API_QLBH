package com.gener.qlbh.repositories;

import com.gener.qlbh.models.OrderSequence;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface OrderSequenceRepository extends JpaRepository<OrderSequence, String> {

    @Modifying
    @Transactional
    @Query(value = "UPDATE order_sequence SET next_val = next_val + 1 WHERE period = :period", nativeQuery = true)
    int increment(@Param("period") String period);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO order_sequence(period, next_val) VALUES (:period, 1)", nativeQuery = true)
    void insertInitial(@Param("period") String period) throws DataIntegrityViolationException;

    @Query(value = "SELECT next_val FROM order_sequence WHERE period = :period", nativeQuery = true)
    Long findNextVal(@Param("period") String period);
}
