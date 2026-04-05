package com.gener.qlbh.repositories;

import com.gener.qlbh.dtos.response.AnalysisRes;
import com.gener.qlbh.dtos.response.RevenueBucketRes;
import com.gener.qlbh.interfaces.RevenueBucket;
import com.gener.qlbh.models.Category;
import com.gener.qlbh.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;


public interface OrderRepository extends JpaRepository<Order,Long> {
    List<Order> findByCreatedAtBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);

    List<Order> findByCustomer_IdAndRemainingAmountGreaterThanOrderByCreatedAtDesc(
            Long customerId, double remainingAmount);

    @Query(value = """
    SELECT * FROM orders o ORDER BY o.created_at DESC LIMIT :amount
""", nativeQuery = true)
    List<Order> findOrdersRecent(@Param("amount") Long amount);

    @Query("""
        SELECT new com.gener.qlbh.dtos.response.AnalysisRes(

            /* ===== SỐ ĐƠN ===== */
            (SELECT COUNT(o1.id)
             FROM Order o1
             WHERE o1.createdAt BETWEEN :start AND :end),

            /* ===== DOANH THU (CÓ THUẾ) ===== */
            (SELECT COALESCE(SUM(
                o2.subtotal
                + o2.shippingFee
                + CASE
                    WHEN o2.tax IS NOT NULL AND o2.tax > 0
                        THEN o2.subtotal * o2.tax / 100
                    ELSE 0
                  END
            ), 0)
             FROM Order o2
             WHERE o2.createdAt BETWEEN :start AND :end),

            /* ===== LỢI NHUẬN GỘP (KHÔNG TÍNH THUẾ, KHÔNG SHIP) ===== */
            (SELECT COALESCE(SUM(

                /* ===== DOANH THU DÒNG ===== */
                (
                    COALESCE(d.price,0)
                    *
                    (
                        CASE
                            WHEN d.length IS NULL OR d.length = 0
                                THEN COALESCE(d.quantity,0)
                            ELSE COALESCE(d.length,0) * COALESCE(d.quantity,0)
                        END
                    )
                )

                /* ===== TRỪ GIÁ VỐN ===== */
                -
                (
                    (
                        CASE
                            WHEN d.length IS NULL OR d.length = 0
                                THEN COALESCE(d.quantity,0)
                            ELSE COALESCE(d.length,0) * COALESCE(d.quantity,0)
                        END
                    )
                    *
                    COALESCE(inv.costPrice,0)
                )

            ), 0)
             FROM Order o3
             JOIN o3.details d
             LEFT JOIN InventoryLot inv ON inv.id = d.inventory.id
             WHERE o3.createdAt BETWEEN :start AND :end),

            /* ===== ĐÃ THU ===== */
            (SELECT COALESCE(SUM(o4.paidAmount),0)
             FROM Order o4
             WHERE o4.createdAt BETWEEN :start AND :end),

            /* ===== CÒN NỢ ===== */
            (SELECT COALESCE(SUM(o5.remainingAmount),0)
             FROM Order o5
             WHERE o5.createdAt BETWEEN :start AND :end),

            /* ===== PHÍ SHIP ===== */
            (SELECT COALESCE(SUM(o6.shippingFee),0)
             FROM Order o6
             WHERE o6.createdAt BETWEEN :start AND :end)

        )
        FROM Order o
        WHERE o.id = (
            SELECT MIN(oo.id)
            FROM Order oo
            WHERE oo.createdAt BETWEEN :start AND :end
        )
    """)
    AnalysisRes calculateSalesReport(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );





    // ====== BUCKET THEO NGÀY ======
    @Query("""
        SELECT new com.gener.qlbh.dtos.response.RevenueBucketRes(
            CAST(FUNCTION('date_trunc','day', o.createdAt) AS timestamp),
            COUNT(DISTINCT o.id),
            SUM(COALESCE(o.subtotal,0.0) + COALESCE(o.shippingFee,0.0))
        )
        FROM Order o
        WHERE o.createdAt >= :start AND o.createdAt < :end
        GROUP BY CAST(FUNCTION('date_trunc','day', o.createdAt) AS timestamp)
        ORDER BY CAST(FUNCTION('date_trunc','day', o.createdAt) AS timestamp)
    """)
    List<RevenueBucketRes> revenueByDay(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // ====== TUẦN ======
    @Query("""
        SELECT new com.gener.qlbh.dtos.response.RevenueBucketRes(
            CAST(FUNCTION('date_trunc','week', o.createdAt) AS timestamp),
            COUNT(DISTINCT o.id),
            SUM(COALESCE(o.subtotal,0.0) + COALESCE(o.shippingFee,0.0))
        )
        FROM Order o
        WHERE o.createdAt >= :start AND o.createdAt < :endExclusive
        GROUP BY CAST(FUNCTION('date_trunc','week', o.createdAt) AS timestamp)
        ORDER BY CAST(FUNCTION('date_trunc','week', o.createdAt) AS timestamp)
    """)
    List<RevenueBucketRes> revenueByWeek(
            @Param("start") LocalDateTime start,
            @Param("endExclusive") LocalDateTime endExclusive
    );

    // ====== THÁNG ======
    @Query("""
        SELECT new com.gener.qlbh.dtos.response.RevenueBucketRes(
            CAST(FUNCTION('date_trunc','month', o.createdAt) AS timestamp),
            COUNT(DISTINCT o.id),
            SUM(COALESCE(o.subtotal,0.0) + COALESCE(o.shippingFee,0.0))
        )
        FROM Order o
        WHERE o.createdAt >= :start AND o.createdAt < :endExclusive
        GROUP BY CAST(FUNCTION('date_trunc','month', o.createdAt) AS timestamp)
        ORDER BY CAST(FUNCTION('date_trunc','month', o.createdAt) AS timestamp)
    """)
    List<RevenueBucketRes> revenueByMonth(
            @Param("start") LocalDateTime start,
            @Param("endExclusive") LocalDateTime endExclusive
    );

    // ====== QUÝ ======
    @Query("""
        SELECT new com.gener.qlbh.dtos.response.RevenueBucketRes(
            CAST(FUNCTION('date_trunc','quarter', o.createdAt) AS timestamp),
            COUNT(DISTINCT o.id),
            SUM(COALESCE(o.subtotal,0.0) + COALESCE(o.shippingFee,0.0))
        )
        FROM Order o
        WHERE o.createdAt >= :start AND o.createdAt < :endExclusive
        GROUP BY CAST(FUNCTION('date_trunc','quarter', o.createdAt) AS timestamp)
        ORDER BY CAST(FUNCTION('date_trunc','quarter', o.createdAt) AS timestamp)
    """)
    List<RevenueBucketRes> revenueByQuarter(
            @Param("start") LocalDateTime start,
            @Param("endExclusive") LocalDateTime endExclusive
    );

    // ====== NĂM ======
    @Query("""
        SELECT new com.gener.qlbh.dtos.response.RevenueBucketRes(
            CAST(FUNCTION('date_trunc','year', o.createdAt) AS timestamp),
            COUNT(DISTINCT o.id),
            SUM(COALESCE(o.subtotal,0.0) + COALESCE(o.shippingFee,0.0))
        )
        FROM Order o
        WHERE o.createdAt >= :start AND o.createdAt < :endExclusive
        GROUP BY CAST(FUNCTION('date_trunc','year', o.createdAt) AS timestamp)
        ORDER BY CAST(FUNCTION('date_trunc','year', o.createdAt) AS timestamp)
    """)
    List<RevenueBucketRes> revenueByYear(
            @Param("start") LocalDateTime start,
            @Param("endExclusive") LocalDateTime endExclusive
    );
}
