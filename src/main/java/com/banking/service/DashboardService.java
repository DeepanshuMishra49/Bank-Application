package com.banking.service;

import com.banking.dto.response.DashboardResponse;

import java.util.UUID;

/**
 * Service interface for dashboard analytics aggregation.
 */
public interface DashboardService {

    /**
     * Returns aggregated analytics for the admin dashboard.
     *
     * @return populated DashboardResponse with system-wide metrics
     */
    DashboardResponse getAdminDashboard();

    /**
     * Returns aggregated analytics for the employee dashboard.
     *
     * @return employee-facing dashboard data
     */
    DashboardResponse getEmployeeDashboard();

    /**
     * Returns analytics for a customer's personal dashboard.
     *
     * @param customerId the customer's UUID
     * @return customer-specific dashboard data
     */
    DashboardResponse getCustomerDashboard(UUID customerId);
}
