package org.example.service;

import org.example.dto.SalesReportResponse;
import org.example.dto.RevenueReportResponse;
import org.example.dto.RevenueByModelReportResponse;
import org.example.dto.RevenueByStaffReportResponse;
import org.example.dto.CarImportCostReportResponse;

import java.time.LocalDateTime;

public interface ReportService {

    /**
     * Tạo báo cáo tổng quát trong khoảng thời gian
     */
    SalesReportResponse generateSalesReport(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Tạo báo cáo doanh thu theo tháng
     */
    RevenueReportResponse generateMonthlyRevenueReport(Integer year, Integer month);

    /**
     * Tạo báo cáo doanh thu theo quý
     */
    RevenueReportResponse generateQuarterlyRevenueReport(Integer year, Integer quarter);

    /**
     * Tạo báo cáo doanh thu theo năm
     */
    RevenueReportResponse generateYearlyRevenueReport(Integer year);

    /**
     * Tạo báo cáo doanh thu theo model theo tháng
     */
    RevenueByModelReportResponse generateRevenueByModelMonthlyReport(Integer year, Integer month);

    /**
     * Tạo báo cáo doanh thu theo model theo quý
     */
    RevenueByModelReportResponse generateRevenueByModelQuarterlyReport(Integer year, Integer quarter);

    /**
     * Tạo báo cáo doanh thu theo model theo năm
     */
    RevenueByModelReportResponse generateRevenueByModelYearlyReport(Integer year);

    /**
     * Tạo báo cáo doanh thu theo staff theo tháng
     */
    RevenueByStaffReportResponse generateRevenueByStaffMonthlyReport(Integer year, Integer month);

    /**
     * Tạo báo cáo doanh thu theo staff theo quý
     */
    RevenueByStaffReportResponse generateRevenueByStaffQuarterlyReport(Integer year, Integer quarter);

    /**
     * Tạo báo cáo doanh thu theo staff theo năm
     */
    RevenueByStaffReportResponse generateRevenueByStaffYearlyReport(Integer year);

    /**
     * Tạo báo cáo chi phí nhập xe theo tháng
     */
    CarImportCostReportResponse generateMonthlyImportCostReport(Integer year, Integer month);

    /**
     * Tạo báo cáo chi phí nhập xe theo quý
     */
    CarImportCostReportResponse generateQuarterlyImportCostReport(Integer year, Integer quarter);

    /**
     * Tạo báo cáo chi phí nhập xe theo năm
     */
    CarImportCostReportResponse generateYearlyImportCostReport(Integer year);
}
