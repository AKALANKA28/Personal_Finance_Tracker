package com.example.finance_tracker.controller;

import com.example.finance_tracker.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;


    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/spending-trend/{userId}")
    public ResponseEntity<Map<String, Object>> generateSpendingTrendReport(
            @PathVariable String userId,
            @RequestParam String startDate,
            @RequestParam String endDate) throws ParseException {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date start = dateFormat.parse(startDate);
        Date end = dateFormat.parse(endDate);

        Map<String, Object> report = reportService.generateSpendingTrendReport(userId, start, end);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/income-vs-expense/{userId}")
    public ResponseEntity<Map<String, Object>> generateIncomeVsExpenseReport(
            @PathVariable String userId,
            @RequestParam String startDate,
            @RequestParam String endDate) throws ParseException {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date start = dateFormat.parse(startDate);
        Date end = dateFormat.parse(endDate);

        Map<String, Object> report = reportService.generateIncomeVsExpenseReport(userId, start, end);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/category-wise/{userId}")
    public ResponseEntity<Map<String, Object>> generateCategoryWiseReport(
            @PathVariable String userId,
            @RequestParam String category,
            @RequestParam String startDate,
            @RequestParam String endDate) throws ParseException {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date start = dateFormat.parse(startDate);
        Date end = dateFormat.parse(endDate);

        Map<String, Object> report = reportService.generateCategoryWiseReport(userId, category, start, end);
        return ResponseEntity.ok(report);
    }
}