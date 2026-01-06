package com.gener.qlbh.services;

import com.gener.qlbh.dtos.response.AnalysisRes;
import com.gener.qlbh.dtos.response.RevenueBucketRes;
import com.gener.qlbh.dtos.response.SalesAnalysisResponse;
import com.gener.qlbh.enums.SuccessCode;
import com.gener.qlbh.interfaces.RevenueBucket;
import com.gener.qlbh.models.Order;
import com.gener.qlbh.models.ResponseObject;
import com.gener.qlbh.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalysisService {


    private final OrderRepository orderRepository;

    public ResponseEntity<ResponseObject> getDateSummary(LocalDate startDate, LocalDate endDate) {

        LocalDateTime startOfDay = startDate.atStartOfDay();

        LocalDateTime endOfDay = endDate.atTime(LocalTime.MAX);

//        AnalysisRes analysisRes = orderRepository.calculateSalesReport(startOfDay,endOfDay);

//        List<RevenueBucketRes> revenueBucket = orderRepository.revenueByDay(startOfDay,endOfDay);

        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Analysis Date Successfully")
                        .data(
                                SalesAnalysisResponse.builder()
                                        .analysisRes(null)
                                        .buckets(null)
                                        .build()
                        )
                        .build()
        );

    }

    public ResponseEntity<ResponseObject> getWeeklySummary(LocalDate startDate, LocalDate endDate) {

        LocalDate startOfWeek = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        LocalDate endOfWeek = endDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        LocalDateTime startDateTime = startOfWeek.atStartOfDay();

        LocalDateTime endDateTime = endOfWeek.atTime(LocalTime.MAX);


//        AnalysisRes analysisRes = orderRepository.calculateSalesReport(startDateTime,endDateTime);
//
//        List<RevenueBucketRes> revenueBucket = orderRepository.revenueByWeek(startDateTime,endDateTime);

        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Weekly Analysis Date Successfully")
                        .data(
                                SalesAnalysisResponse.builder()
                                        .analysisRes(null)
                                        .buckets(null)
                                        .build()
                        )
                        .build()
        );
    }

    public ResponseEntity<ResponseObject> getMonthlySummary(LocalDate startDate, LocalDate endDate) {
        LocalDate startOfMonth = startDate.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = endDate.with(TemporalAdjusters.lastDayOfMonth());
        LocalDateTime startDateTime = startOfMonth.atStartOfDay();
        LocalDateTime endDateTime = endOfMonth.atTime(LocalTime.MAX);

//        AnalysisRes analysisRes = orderRepository.calculateSalesReport(startDateTime,endDateTime);

//        List<RevenueBucketRes> revenueBucket = orderRepository.revenueByMonth(startDateTime,endDateTime);

        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Monthly Analysis Date Successfully")
                        .data(
                                SalesAnalysisResponse.builder()
                                        .analysisRes(null)
                                        .buckets(null)
                                        .build()
                        )
                        .build()
        );
    }

    public ResponseEntity<ResponseObject> getQuarterlySummary(LocalDate startDate, LocalDate endDate) {

        Month startFirstMonth = startDate.getMonth().firstMonthOfQuarter();
        LocalDate startOfQuarter = LocalDate.of(startDate.getYear(), startFirstMonth, 1);


        Month endFirstMonth = endDate.getMonth().firstMonthOfQuarter();
        LocalDate endOfQuarter = LocalDate.of(endDate.getYear(), endFirstMonth.plus(2), 1)
                .with(TemporalAdjusters.lastDayOfMonth());

        LocalDateTime startDateTime = startOfQuarter.atStartOfDay();
        LocalDateTime endDateTime   = endOfQuarter.atTime(LocalTime.MAX); // inclusive


//        AnalysisRes analysisRes = orderRepository.calculateSalesReport(startDateTime, endDateTime);


//        List<RevenueBucketRes> revenueBucket = orderRepository.revenueByQuarter(startDateTime, endDateTime);

        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Quarterly Analysis Date Successfully")
                        .data(
                                SalesAnalysisResponse.builder()
                                        .analysisRes(null)
                                        .buckets(null)
                                        .build()
                        )
                        .build()
        );
    }

    public ResponseEntity<ResponseObject> getYearlySummary(LocalDate startDate, LocalDate endDate) {
        LocalDate startOfYear = startDate.with(TemporalAdjusters.firstDayOfYear());
        LocalDate endOfYear = endDate.with(TemporalAdjusters.lastDayOfYear());
        LocalDateTime startDateTime = startOfYear.atStartOfDay();
        LocalDateTime endDateTime = endOfYear.atTime(LocalTime.MAX);
//        AnalysisRes analysisRes = orderRepository.calculateSalesReport(startDateTime,endDateTime);

//        List<RevenueBucketRes> revenueBucket = orderRepository.revenueByYear(startDateTime,endDateTime);

        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode()).body(
                ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Yearly Analysis Date Successfully")
                        .data(
                                SalesAnalysisResponse.builder()
                                        .analysisRes(null)
                                        .buckets(null)
                                        .build()
                        )
                        .build()
        );
    }
}
