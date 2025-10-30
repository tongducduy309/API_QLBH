package com.gener.qlbh.dtos.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RevenueBucketRes {
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDateTime periodStart;
    private Long totalOrders;
    private Double totalRevenue;       // subtotal + shippingFee

    public RevenueBucketRes(Timestamp periodStart, Long totalOrders, Double totalRevenue) {
        this.periodStart = (periodStart == null) ? null : periodStart.toLocalDateTime();
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
    }

    // Khớp khi tổng trả về BigDecimal
    public RevenueBucketRes(Timestamp periodStart, Long totalOrders, BigDecimal totalRevenue) {
        this.periodStart = (periodStart == null) ? null : periodStart.toLocalDateTime();
        this.totalOrders = totalOrders;
        this.totalRevenue = (totalRevenue == null) ? null : totalRevenue.doubleValue();
    }
}
