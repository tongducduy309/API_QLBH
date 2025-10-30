package com.gener.qlbh.interfaces;

import java.time.LocalDateTime;

public interface RevenueBucket {
    LocalDateTime getPeriodStart(); // mốc đầu kỳ (00:00)
    Long getTotalOrders();                    // số đơn trong kỳ
    Double getTotalRevenue();                 // doanh thu = subtotal + shippingFee
}
