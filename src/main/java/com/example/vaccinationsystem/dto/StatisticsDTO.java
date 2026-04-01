package com.example.vaccinationsystem.dto;

import java.util.List;
import java.util.Map;

public class StatisticsDTO {
    // Admin Stats
    private Double totalRevenue;
    private List<Map<String, Object>> topVaccines;
    
    // Doctor Stats
    private List<Map<String, Object>> upcomingVaccinations;

    // Inventory Stats
    private List<Map<String, Object>> lowStockVaccines;
    
    // Cashier Stats
    private Double todayRevenue;
    private Long todayBillsCount;

    // Getters and Setters
    public Double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }

    public List<Map<String, Object>> getTopVaccines() { return topVaccines; }
    public void setTopVaccines(List<Map<String, Object>> topVaccines) { this.topVaccines = topVaccines; }

    public List<Map<String, Object>> getUpcomingVaccinations() { return upcomingVaccinations; }
    public void setUpcomingVaccinations(List<Map<String, Object>> upcomingVaccinations) { this.upcomingVaccinations = upcomingVaccinations; }

    public List<Map<String, Object>> getLowStockVaccines() { return lowStockVaccines; }
    public void setLowStockVaccines(List<Map<String, Object>> lowStockVaccines) { this.lowStockVaccines = lowStockVaccines; }

    public Double getTodayRevenue() { return todayRevenue; }
    public void setTodayRevenue(Double todayRevenue) { this.todayRevenue = todayRevenue; }

    public Long getTodayBillsCount() { return todayBillsCount; }
    public void setTodayBillsCount(Long todayBillsCount) { this.todayBillsCount = todayBillsCount; }
}
