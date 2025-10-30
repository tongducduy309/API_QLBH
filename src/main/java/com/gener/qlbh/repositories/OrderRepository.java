package com.gener.qlbh.repositories;

import com.gener.qlbh.dtos.response.AnalysisRes;
import com.gener.qlbh.dtos.response.RevenueBucketRes;
import com.gener.qlbh.interfaces.RevenueBucket;
import com.gener.qlbh.models.Category;
import com.gener.qlbh.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;


public interface OrderRepository extends JpaRepository<Order,String> {
//    List<Order> findByOrderDateBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);

    List<Order> findByCustomer_IdAndRemainingAmountGreaterThanOrderByCreatedAtDesc(
            Long customerId, double remainingAmount);

    @Query(value= """
    SELECT new com.gener.qlbh.dtos.response.AnalysisRes(
                 /* Số đơn */
                 (SELECT COUNT(oo.id)
                    FROM Order oo
                   WHERE oo.createdAt BETWEEN :startDateTime AND :endDateTime),
            
                 /* Doanh thu = subtotal + shippingFee (cấp-đơn) */
                 (SELECT SUM(COALESCE(oo.subtotal,0.0) + COALESCE(oo.shippingFee,0.0))
                    FROM Order oo
                   WHERE oo.createdAt BETWEEN :startDateTime AND :endDateTime),
            
                 /* Lợi nhuận gộp = gp_dich_vu + gp_hang */
                 COALESCE((
                     SELECT SUM(COALESCE(d.price,0.0) * COALESCE(d.quantity,0.0))
                     FROM Order o1
                     JOIN o1.details d
                     WHERE o1.createdAt BETWEEN :startDateTime AND :endDateTime
                       AND d.product IS NULL
                 ), 0.0)
                 +
                 COALESCE((
                     SELECT SUM(
                         CASE
                             WHEN p2.category.method = com.gener.qlbh.enums.Method.SHEET_METAL
                                 THEN COALESCE(d2.length,0.0) * COALESCE(d2.quantity,0.0)
                                      * (COALESCE(d2.price,0.0) - COALESCE(p2.costPrice,0.0))
                             ELSE COALESCE(d2.quantity,0.0)
                                  * (COALESCE(d2.price,0.0) - COALESCE(p2.costPrice,0.0))
                         END
                     )
                     FROM Order o2
                     JOIN o2.details d2
                     JOIN d2.product p2
                     WHERE o2.createdAt BETWEEN :startDateTime AND :endDateTime
                 ), 0.0),
            
                 /* Đã thu */
                 (SELECT SUM(COALESCE(oo.paidAmount,0.0))
                    FROM Order oo
                   WHERE oo.createdAt BETWEEN :startDateTime AND :endDateTime),
            
                 /* Còn nợ */
                 (SELECT SUM(COALESCE(oo.remainingAmount,0.0))
                    FROM Order oo
                   WHERE oo.createdAt BETWEEN :startDateTime AND :endDateTime),
            
                 /* Phí ship */
                 (SELECT SUM(COALESCE(oo.shippingFee,0.0))
                    FROM Order oo
                   WHERE oo.createdAt BETWEEN :startDateTime AND :endDateTime)
             )
             FROM Order o
             /* ràng buộc để trả đúng 1 dòng */
             WHERE o.id = (SELECT MIN(oo.id) FROM Order oo WHERE oo.createdAt BETWEEN :startDateTime AND :endDateTime)
   \s""") // Nhóm theo ngày (chỉ cần nếu tính nhiều ngày)
    AnalysisRes calculateSalesReport(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
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
