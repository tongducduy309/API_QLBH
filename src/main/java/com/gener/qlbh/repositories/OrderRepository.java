package com.gener.qlbh.repositories;

import com.gener.qlbh.dtos.response.AnalysisRes;
import com.gener.qlbh.dtos.response.RevenueBucketRes;
import com.gener.qlbh.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCreatedAtBetween(LocalDate start, LocalDate end);

    List<Order> findByCustomer_IdAndRemainingAmountGreaterThanOrderByCreatedAtDesc(
            Long customerId, double remainingAmount
    );

    @Query(value = """
    SELECT *
    FROM orders o
    WHERE o.status = 'CONFIRMED'
    ORDER BY o.created_at DESC
    LIMIT :amount
""", nativeQuery = true)
    List<Order> findOrdersRecent(@Param("amount") Long amount);

    @Query("""
        SELECT new com.gener.qlbh.dtos.response.AnalysisRes(
            COUNT(o),
            COALESCE(SUM(
                COALESCE(o.subtotal, 0)
                + COALESCE(o.shippingFee, 0)
                + (COALESCE(o.subtotal, 0) * COALESCE(o.tax, 0) / 100.0)
            ), 0),
            0.0,
            COALESCE(SUM(COALESCE(o.paidAmount, 0)), 0),
            COALESCE(SUM(COALESCE(o.remainingAmount, 0)), 0),
            COALESCE(SUM(COALESCE(o.shippingFee, 0)), 0)
        )
        FROM Order o
        WHERE o.createdAt BETWEEN :start AND :end
          AND o.status = com.gener.qlbh.enums.OrderStatus.CONFIRMED
    """)
    AnalysisRes calculateSalesReport(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("""
        SELECT new com.gener.qlbh.dtos.response.RevenueBucketRes(
            o.createdAt,
            COUNT(DISTINCT o.id),
            COALESCE(SUM(
                COALESCE(o.subtotal, 0.0)
                + COALESCE(o.shippingFee, 0.0)
                + (COALESCE(o.subtotal, 0.0) * COALESCE(o.tax, 0.0) / 100.0)
            ), 0.0)
        )
        FROM Order o
        WHERE o.createdAt BETWEEN :start AND :end
          AND o.status = com.gener.qlbh.enums.OrderStatus.CONFIRMED
        GROUP BY o.createdAt
        ORDER BY o.createdAt
    """)
    List<RevenueBucketRes> revenueByDay(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("""
        SELECT COALESCE(SUM(
            (COALESCE(od.price, 0) - COALESCE(i.costPrice, 0)) *
            (
                CASE
                    WHEN od.length IS NULL OR od.length = 0 THEN COALESCE(od.quantity, 0)
                    ELSE COALESCE(od.quantity, 0) * od.length
                END
            )
        ), 0)
        FROM OrderDetail od
        JOIN od.order o
        LEFT JOIN od.inventory i
        WHERE o.createdAt BETWEEN :start AND :end
          AND o.status = com.gener.qlbh.enums.OrderStatus.CONFIRMED
    """)
    Double calculateGrossProfit(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
}