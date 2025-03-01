package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Report;

import java.util.Date;

public interface ReportService {
    Report generateSpendingTrendReport(String userId, Date startDate, Date endDate);
    Report generateIncomeVsExpenseReport(String userId, Date startDate, Date endDate);
    Report generateCategoryWiseReport(String userId, String category);
}
