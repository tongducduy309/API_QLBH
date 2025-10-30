package com.gener.qlbh.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRes {
    private Long totalOrders; // Số đơn
    private Double totalRevenue; // Doanh thu
    private Double totalGrossProfit; // Lợi nhuận gộp
    private Double totalAmountPaid; // Đã thu
    private Double totalDebt; // Còn nợ (TotalAmount - AmountPaid)
    private Double totalShippingFee; // Phí vận chuyển
}
