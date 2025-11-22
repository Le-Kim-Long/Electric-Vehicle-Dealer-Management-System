package org.example.service;

import org.example.dto.SalesReportResponse;
import org.example.dto.RevenueReportResponse;
import org.example.dto.RevenueByModelReportResponse;
import org.example.dto.RevenueByStaffReportResponse;
import org.example.dto.CarImportCostReportResponse;

import java.time.LocalDateTime;

public interface ReportService {

    /**
     * Tạo báo cáo tổng quát trong khoảng thời gian cho dealer cụ thể
     */
    SalesReportResponse generateSalesReportForDealer(LocalDateTime startDate, LocalDateTime endDate, Integer dealerId);

    /**
     * Tạo báo cáo doanh thu theo tháng cho dealer cụ thể
     */
    RevenueReportResponse generateMonthlyRevenueReportForDealer(Integer year, Integer month, Integer dealerId);

    /**
     * Tạo báo cáo doanh thu theo quý cho dealer cụ thể
     */
    RevenueReportResponse generateQuarterlyRevenueReportForDealer(Integer year, Integer quarter, Integer dealerId);

    /**
     * Tạo báo cáo doanh thu theo năm cho dealer cụ thể
     */
    RevenueReportResponse generateYearlyRevenueReportForDealer(Integer year, Integer dealerId);

    /**
     * Tạo báo cáo doanh thu theo model theo tháng cho dealer cụ thể
     */
    RevenueByModelReportResponse generateRevenueByModelMonthlyReportForDealer(Integer year, Integer month, Integer dealerId);

    /**
     * Tạo báo cáo doanh thu theo model theo quý cho dealer cụ thể
     */
    RevenueByModelReportResponse generateRevenueByModelQuarterlyReportForDealer(Integer year, Integer quarter, Integer dealerId);

    /**
     * Tạo báo cáo doanh thu theo model theo năm cho dealer cụ thể
     */
    RevenueByModelReportResponse generateRevenueByModelYearlyReportForDealer(Integer year, Integer dealerId);

    /**
     * Tạo báo cáo doanh thu theo staff theo tháng cho dealer cụ thể
     */
    RevenueByStaffReportResponse generateRevenueByStaffMonthlyReportForDealer(Integer year, Integer month, Integer dealerId);

    /**
     * Tạo báo cáo doanh thu theo staff theo quý cho dealer cụ thể
     */
    RevenueByStaffReportResponse generateRevenueByStaffQuarterlyReportForDealer(Integer year, Integer quarter, Integer dealerId);

    /**
     * Tạo báo cáo doanh thu theo staff theo năm cho dealer cụ thể
     */
    RevenueByStaffReportResponse generateRevenueByStaffYearlyReportForDealer(Integer year, Integer dealerId);

    /**
     * Tạo báo cáo chi phí nhập xe theo tháng cho dealer cụ thể
     */
    CarImportCostReportResponse generateMonthlyImportCostReportForDealer(Integer year, Integer month, Integer dealerId);

    /**
     * Tạo báo cáo chi phí nhập xe theo quý cho dealer cụ thể
     */
    CarImportCostReportResponse generateQuarterlyImportCostReportForDealer(Integer year, Integer quarter, Integer dealerId);

    /**
     * Tạo báo cáo chi phí nhập xe theo năm cho dealer cụ thể
     */
    CarImportCostReportResponse generateYearlyImportCostReportForDealer(Integer year, Integer dealerId);
}
