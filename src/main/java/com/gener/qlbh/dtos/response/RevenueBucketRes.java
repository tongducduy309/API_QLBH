package com.gener.qlbh.dtos.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class RevenueBucketRes {

    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate periodStart;

    private Long totalOrders;
    private Double totalRevenue;

    public RevenueBucketRes(LocalDate periodStart, Long totalOrders, Double totalRevenue) {
        this.periodStart = periodStart;
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
    }

    public RevenueBucketRes(Timestamp periodStart, Long totalOrders, Double totalRevenue) {
        this.periodStart = (periodStart == null) ? null : periodStart.toLocalDateTime().toLocalDate();
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
    }

    public RevenueBucketRes(Timestamp periodStart, Long totalOrders, BigDecimal totalRevenue) {
        this.periodStart = (periodStart == null) ? null : periodStart.toLocalDateTime().toLocalDate();
        this.totalOrders = totalOrders;
        this.totalRevenue = (totalRevenue == null) ? null : totalRevenue.doubleValue();
    }
}