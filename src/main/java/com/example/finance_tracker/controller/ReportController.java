package com.example.finance_tracker.controller;

import com.example.finance_tracker.model.Report;
import com.example.finance_tracker.service.ReportService;
import com.example.finance_tracker.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/spending-trend/{userId}")
    public ResponseEntity<Map<String, Object>> generateSpendingTrendReport(
            @PathVariable String userId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        Map<String, Object> report = reportService.generateSpendingTrendReport(userId, start, end);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/income-vs-expense/{userId}")
    public ResponseEntity<Map<String, Object>> generateIncomeVsExpenseReport(
            @PathVariable String userId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        Map<String, Object> report = reportService.generateIncomeVsExpenseReport(userId, start, end);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/category-wise/{userId}")
    public ResponseEntity<Map<String, Object>> generateCategoryWiseReport(
            @PathVariable String userId,
            @RequestParam String category,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        Map<String, Object> report = reportService.generateCategoryWiseReport(userId, category, start, end);
        return ResponseEntity.ok(report);
    }
}