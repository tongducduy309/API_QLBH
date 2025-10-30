package com.gener.qlbh.controllers;

import com.gener.qlbh.models.Category;
import com.gener.qlbh.models.ResponseObject;
import com.gener.qlbh.services.AnalysisService;
import com.gener.qlbh.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping(path = "/api/v1/analysis")
@RequiredArgsConstructor
public class AnalysisController {
    private final AnalysisService analysisService;

    @GetMapping("/date-summary")
    ResponseEntity<ResponseObject> getDateSummary(
            @RequestParam("start")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,@RequestParam("end")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate){
        return analysisService.getDateSummary(startDate,endDate);
    }

    @GetMapping("/weekly-summary")
    public ResponseEntity<ResponseObject> getWeeklySummary(
            @RequestParam("start")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,@RequestParam("end")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {
        return analysisService.getWeeklySummary(startDate,endDate);
    }

    @GetMapping("/monthly-summary")
    public ResponseEntity<ResponseObject> getMonthlySummary(
            @RequestParam("start")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,@RequestParam("end")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {
        return analysisService.getMonthlySummary(startDate,endDate);
    }

    @GetMapping("/quarterly-summary")
    public ResponseEntity<ResponseObject> getQuarterlySummary(
            @RequestParam("start")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,@RequestParam("end")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {
        return analysisService.getQuarterlySummary(startDate,endDate);
    }

    @GetMapping("/yearly-summary")
    public ResponseEntity<ResponseObject> getYearlySummary(
            @RequestParam("start")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,@RequestParam("end")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {
        return analysisService.getYearlySummary(startDate,endDate);
    }
}
