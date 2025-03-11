package com.example.finance_tracker.service;

import com.example.finance_tracker.model.Dashboard;

public interface DashboardService {
    Dashboard getAdminDashboardSummary();
    Dashboard getUserDashboardSummary(String userId);
}