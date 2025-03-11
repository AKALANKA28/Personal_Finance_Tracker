package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Budget;
import com.example.finance_tracker.model.Report;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ReportService {

    Map<String, Object> generateSpendingTrendReport(String userId, Date startDate, Date endDate);
    Map<String, Object> generateIncomeVsExpenseReport(String userId, Date startDate, Date endDate);
    Map<String, Object> generateCategoryWiseReport(String userId, String category, Date startDate, Date endDate);

}
