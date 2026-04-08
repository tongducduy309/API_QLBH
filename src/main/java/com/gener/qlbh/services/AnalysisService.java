package com.gener.qlbh.services;

import com.gener.qlbh.dtos.response.AnalysisRes;
import com.gener.qlbh.dtos.response.RevenueBucketRes;
import com.gener.qlbh.dtos.response.SalesAnalysisResponse;
import com.gener.qlbh.enums.SuccessCode;
import com.gener.qlbh.models.ResponseObject;
import com.gener.qlbh.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final OrderRepository orderRepository;

    public ResponseEntity<?> getDateSummary(LocalDate startDate, LocalDate endDate) {
        validateRange(startDate, endDate);

        AnalysisRes analysisRes = orderRepository.calculateSalesReport(startDate, endDate);
        Double grossProfit = orderRepository.calculateGrossProfit(startDate, endDate);
        analysisRes.setTotalGrossProfit(grossProfit == null ? 0d : grossProfit);

        List<RevenueBucketRes> revenueBucket = orderRepository.revenueByDay(startDate, endDate);

        return buildResponse("Analysis date successfully", analysisRes, revenueBucket);
    }

    public ResponseEntity<?> getWeeklySummary(LocalDate startDate, LocalDate endDate) {
        validateRange(startDate, endDate);

        LocalDate startOfWeek = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = endDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        AnalysisRes analysisRes = orderRepository.calculateSalesReport(startOfWeek, endOfWeek);
        Double grossProfit = orderRepository.calculateGrossProfit(startOfWeek, endOfWeek);
        analysisRes.setTotalGrossProfit(grossProfit == null ? 0d : grossProfit);

        List<RevenueBucketRes> revenueBucket = orderRepository.revenueByDay(startOfWeek, endOfWeek);

        return buildResponse("Weekly analysis successfully", analysisRes, revenueBucket);
    }

    public ResponseEntity<?> getMonthlySummary(LocalDate startDate, LocalDate endDate) {
        validateRange(startDate, endDate);

        LocalDate startOfMonth = startDate.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = endDate.with(TemporalAdjusters.lastDayOfMonth());

        AnalysisRes analysisRes = orderRepository.calculateSalesReport(startOfMonth, endOfMonth);
        Double grossProfit = orderRepository.calculateGrossProfit(startOfMonth, endOfMonth);
        analysisRes.setTotalGrossProfit(grossProfit == null ? 0d : grossProfit);

        List<RevenueBucketRes> revenueBucket = orderRepository.revenueByDay(startOfMonth, endOfMonth);

        return buildResponse("Monthly analysis successfully", analysisRes, revenueBucket);
    }

    public ResponseEntity<?> getQuarterlySummary(LocalDate startDate, LocalDate endDate) {
        validateRange(startDate, endDate);

        Month startQuarterMonth = startDate.getMonth().firstMonthOfQuarter();
        LocalDate startOfQuarter = LocalDate.of(startDate.getYear(), startQuarterMonth, 1);

        Month endQuarterMonth = endDate.getMonth().firstMonthOfQuarter();
        LocalDate endOfQuarter = LocalDate.of(endDate.getYear(), endQuarterMonth.plus(2), 1)
                .with(TemporalAdjusters.lastDayOfMonth());

        AnalysisRes analysisRes = orderRepository.calculateSalesReport(startOfQuarter, endOfQuarter);
        Double grossProfit = orderRepository.calculateGrossProfit(startOfQuarter, endOfQuarter);
        analysisRes.setTotalGrossProfit(grossProfit == null ? 0d : grossProfit);

        List<RevenueBucketRes> revenueBucket = orderRepository.revenueByDay(startOfQuarter, endOfQuarter);

        return buildResponse("Quarterly analysis successfully", analysisRes, revenueBucket);
    }

    public ResponseEntity<?> getYearlySummary(LocalDate startDate, LocalDate endDate) {
        validateRange(startDate, endDate);

        LocalDate startOfYear = startDate.with(TemporalAdjusters.firstDayOfYear());
        LocalDate endOfYear = endDate.with(TemporalAdjusters.lastDayOfYear());

        AnalysisRes analysisRes = orderRepository.calculateSalesReport(startOfYear, endOfYear);
        Double grossProfit = orderRepository.calculateGrossProfit(startOfYear, endOfYear);
        analysisRes.setTotalGrossProfit(grossProfit == null ? 0d : grossProfit);

        List<RevenueBucketRes> revenueBucket = orderRepository.revenueByDay(startOfYear, endOfYear);

        return buildResponse("Yearly analysis successfully", analysisRes, revenueBucket);
    }

    private void validateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được để trống");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Ngày bắt đầu không được lớn hơn ngày kết thúc");
        }
    }

    private ResponseEntity<ResponseObject> buildResponse(
            String message,
            AnalysisRes analysisRes,
            List<RevenueBucketRes> buckets
    ) {
        SalesAnalysisResponse response = SalesAnalysisResponse.builder()
                .analysisRes(analysisRes)
                .buckets(buckets)
                .build();

        return ResponseEntity
                .status(SuccessCode.REQUEST.getHttpStatusCode())
                .body(
                        ResponseObject.builder()
                                .status(SuccessCode.REQUEST.getStatus())
                                .message(message)
                                .data(response)
                                .build()
                );
    }
}