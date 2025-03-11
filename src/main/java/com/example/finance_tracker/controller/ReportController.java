package com.example.finance_tracker.controller;

import com.example.finance_tracker.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Report Controller", description = "APIs for generating financial reports")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/spending-trend/{userId}")
    @Operation(
            summary = "Generate spending trend report",
            description = "Generate a spending trend report for a specific user within a date range."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Spending trend report generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date format provided"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> generateSpendingTrendReport(
            @Parameter(description = "ID of the user to generate the report for", required = true)
            @PathVariable String userId,
            @Parameter(description = "Start date (format: yyyy-MM-dd)", required = true)
            @RequestParam String startDate,
            @Parameter(description = "End date (format: yyyy-MM-dd)", required = true)
            @RequestParam String endDate) throws ParseException {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date start = dateFormat.parse(startDate);
        Date end = dateFormat.parse(endDate);

        Map<String, Object> report = reportService.generateSpendingTrendReport(userId, start, end);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/income-vs-expense/{userId}")
    @Operation(
            summary = "Generate income vs expense report",
            description = "Generate an income vs expense report for a specific user within a date range."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Income vs expense report generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date format provided"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> generateIncomeVsExpenseReport(
            @Parameter(description = "ID of the user to generate the report for", required = true)
            @PathVariable String userId,
            @Parameter(description = "Start date (format: yyyy-MM-dd)", required = true)
            @RequestParam String startDate,
            @Parameter(description = "End date (format: yyyy-MM-dd)", required = true)
            @RequestParam String endDate) throws ParseException {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date start = dateFormat.parse(startDate);
        Date end = dateFormat.parse(endDate);

        Map<String, Object> report = reportService.generateIncomeVsExpenseReport(userId, start, end);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/category-wise/{userId}")
    @Operation(
            summary = "Generate category-wise report",
            description = "Generate a category-wise report for a specific user within a date range."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category-wise report generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date format provided"),
            @ApiResponse(responseCode = "404", description = "User or category not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> generateCategoryWiseReport(
            @Parameter(description = "ID of the user to generate the report for", required = true)
            @PathVariable String userId,
            @Parameter(description = "Category to filter the report by", required = true)
            @RequestParam String category,
            @Parameter(description = "Start date (format: yyyy-MM-dd)", required = true)
            @RequestParam String startDate,
            @Parameter(description = "End date (format: yyyy-MM-dd)", required = true)
            @RequestParam String endDate) throws ParseException {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date start = dateFormat.parse(startDate);
        Date end = dateFormat.parse(endDate);

        Map<String, Object> report = reportService.generateCategoryWiseReport(userId, category, start, end);
        return ResponseEntity.ok(report);
    }
}