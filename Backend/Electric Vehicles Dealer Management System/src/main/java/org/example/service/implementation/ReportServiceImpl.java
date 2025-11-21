package org.example.service.implementation;

import lombok.RequiredArgsConstructor;
import org.example.dto.SalesReportResponse;
import org.example.dto.RevenueReportResponse;
import org.example.dto.RevenueByModelReportResponse;
import org.example.dto.RevenueByStaffReportResponse;
import org.example.dto.CarImportCostReportResponse;
import org.example.repository.OrdersRepository;
import org.example.repository.CarDistributionRequestRepository;
import org.example.service.ReportService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final OrdersRepository ordersRepository;
    private final CarDistributionRequestRepository carDistributionRequestRepository;

    /**
     * Tạo báo cáo tổng quát trong khoảng thời gian
     */
    public SalesReportResponse generateSalesReport(LocalDateTime startDate, LocalDateTime endDate) {

        // Tổng đơn hàng đã thanh toán (tính theo completedDate)
        Long totalCompletedOrders = ordersRepository.countCompletedOrdersByCompletedDateRange(
            startDate, endDate
        );

        // Tổng đơn hàng chưa hoàn thành (tính theo orderDate)
        Long totalPendingOrders = ordersRepository.countPendingOrdersByOrderDateRange(
            startDate, endDate
        );

        // Tổng xe bán được từ đơn hàng đã thanh toán
        Long totalCarsSold = ordersRepository.countTotalCarsSoldByCompletedDateRange(
            startDate, endDate
        );

        // Tổng doanh thu từ đơn hàng đã thanh toán
        BigDecimal totalRevenue = ordersRepository.sumTotalRevenueByCompletedDateRange(
            startDate, endDate
        );

        // Tổng lợi nhuận (dealer price - manufacturer price)
        BigDecimal totalProfit = ordersRepository.sumTotalProfitByCompletedDateRange(
            startDate, endDate
        );

        // Tổng số xe nhập vào từ các yêu cầu phân phối đã giao
        Long totalCarsDistributed = carDistributionRequestRepository.sumTotalCarsDistributedByDeliveryDateRange(
            startDate, endDate
        );

        return SalesReportResponse.builder()
            .totalCompletedOrders(totalCompletedOrders != null ? totalCompletedOrders : 0L)
            .totalPendingOrders(totalPendingOrders != null ? totalPendingOrders : 0L)
            .totalCarsSold(totalCarsSold != null ? totalCarsSold : 0L)
            .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
            .totalProfit(totalProfit != null ? totalProfit : BigDecimal.ZERO)
            .totalCarsDistributed(totalCarsDistributed != null ? totalCarsDistributed : 0L)
            .build();
    }

    /**
     * Tạo báo cáo doanh thu theo tháng
     */
    public RevenueReportResponse generateMonthlyRevenueReport(Integer year, Integer month) {
        // Lấy tổng doanh thu của tháng
        BigDecimal totalRevenue = ordersRepository.getTotalRevenueByMonth(year, month);

        // Lấy doanh thu theo từng ngày trong tháng
        List<Object[]> dailyRevenueData = ordersRepository.getDailyRevenueByMonth(year, month);

        // Xác định số ngày trong tháng
        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();

        // Tạo map để dễ dàng tra cứu doanh thu theo ngày
        Map<Integer, BigDecimal> revenueMap = new HashMap<>();
        for (Object[] row : dailyRevenueData) {
            Integer day = (Integer) row[0];
            BigDecimal revenue = (BigDecimal) row[1];
            revenueMap.put(day, revenue);
        }

        // Tạo danh sách chi tiết doanh thu cho tất cả các ngày trong tháng
        List<RevenueReportResponse.RevenueDetail> revenueDetails = new ArrayList<>();
        for (int day = 1; day <= daysInMonth; day++) {
            BigDecimal dayRevenue = revenueMap.getOrDefault(day, BigDecimal.ZERO);
            revenueDetails.add(RevenueReportResponse.RevenueDetail.builder()
                    .periodName("Ngày " + day)
                    .periodNumber(day)
                    .revenue(dayRevenue)
                    .build());
        }

        return RevenueReportResponse.builder()
                .reportType("MONTHLY")
                .year(year)
                .month(month)
                .totalRevenue(totalRevenue)
                .revenueDetails(revenueDetails)
                .build();
    }

    /**
     * Tạo báo cáo doanh thu theo quý
     */
    public RevenueReportResponse generateQuarterlyRevenueReport(Integer year, Integer quarter) {
        // Tính tháng bắt đầu và kết thúc của quý
        int startMonth = (quarter - 1) * 3 + 1;
        int endMonth = startMonth + 2;

        // Lấy tổng doanh thu của quý
        BigDecimal totalRevenue = ordersRepository.getTotalRevenueByQuarter(year, startMonth, endMonth);

        // Lấy doanh thu theo từng tháng trong quý
        List<Object[]> monthlyRevenueData = ordersRepository.getMonthlyRevenueByQuarter(year, startMonth, endMonth);

        // Tạo map để dễ dàng tra cứu doanh thu theo tháng
        Map<Integer, BigDecimal> revenueMap = new HashMap<>();
        for (Object[] row : monthlyRevenueData) {
            Integer month = (Integer) row[0];
            BigDecimal revenue = (BigDecimal) row[1];
            revenueMap.put(month, revenue);
        }

        // Tạo danh sách chi tiết doanh thu cho 3 tháng trong quý
        List<RevenueReportResponse.RevenueDetail> revenueDetails = new ArrayList<>();
        String[] monthNames = {"", "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
                              "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"};

        for (int month = startMonth; month <= endMonth; month++) {
            BigDecimal monthRevenue = revenueMap.getOrDefault(month, BigDecimal.ZERO);
            int quarterMonth = month - startMonth + 1; // 1, 2, 3
            revenueDetails.add(RevenueReportResponse.RevenueDetail.builder()
                    .periodName(monthNames[month])
                    .periodNumber(quarterMonth)
                    .revenue(monthRevenue)
                    .build());
        }

        return RevenueReportResponse.builder()
                .reportType("QUARTERLY")
                .year(year)
                .quarter(quarter)
                .totalRevenue(totalRevenue)
                .revenueDetails(revenueDetails)
                .build();
    }

    /**
     * Tạo báo cáo doanh thu theo năm
     */
    public RevenueReportResponse generateYearlyRevenueReport(Integer year) {
        // Lấy tổng doanh thu của năm
        BigDecimal totalRevenue = ordersRepository.getTotalRevenueByYear(year);

        // Lấy doanh thu theo từng tháng trong năm
        List<Object[]> monthlyRevenueData = ordersRepository.getMonthlyRevenueByYear(year);

        // Tạo map để dễ dàng tra cứu doanh thu theo tháng
        Map<Integer, BigDecimal> revenueMap = new HashMap<>();
        for (Object[] row : monthlyRevenueData) {
            Integer month = (Integer) row[0];
            BigDecimal revenue = (BigDecimal) row[1];
            revenueMap.put(month, revenue);
        }

        // Tạo danh sách chi tiết doanh thu cho 12 tháng trong năm
        List<RevenueReportResponse.RevenueDetail> revenueDetails = new ArrayList<>();
        String[] monthNames = {"", "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
                              "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"};

        for (int month = 1; month <= 12; month++) {
            BigDecimal monthRevenue = revenueMap.getOrDefault(month, BigDecimal.ZERO);
            revenueDetails.add(RevenueReportResponse.RevenueDetail.builder()
                    .periodName(monthNames[month])
                    .periodNumber(month)
                    .revenue(monthRevenue)
                    .build());
        }

        return RevenueReportResponse.builder()
                .reportType("YEARLY")
                .year(year)
                .totalRevenue(totalRevenue)
                .revenueDetails(revenueDetails)
                .build();
    }

    /**
     * Tạo báo cáo doanh thu theo model theo tháng
     */
    public RevenueByModelReportResponse generateRevenueByModelMonthlyReport(Integer year, Integer month) {
        // Lấy doanh thu theo model trong tháng
        List<Object[]> modelRevenueData = ordersRepository.getRevenueByModelByMonth(year, month);

        // Lấy tổng doanh thu từ finalPrice trong tháng
        BigDecimal totalRevenue = ordersRepository.getTotalRevenueFromFinalPriceByMonth(year, month);

        return buildRevenueByModelReport(modelRevenueData, totalRevenue, "MONTHLY",
                                       year + "-" + String.format("%02d", month) + "-01",
                                       year + "-" + String.format("%02d", month) + "-" +
                                       YearMonth.of(year, month).lengthOfMonth());
    }

    /**
     * Tạo báo cáo doanh thu theo model theo quý
     */
    public RevenueByModelReportResponse generateRevenueByModelQuarterlyReport(Integer year, Integer quarter) {
        // Tính tháng bắt đầu và kết thúc của quý
        int startMonth = (quarter - 1) * 3 + 1;
        int endMonth = startMonth + 2;

        // Lấy doanh thu theo model trong quý
        List<Object[]> modelRevenueData = ordersRepository.getRevenueByModelByQuarter(year, startMonth, endMonth);

        // Lấy tổng doanh thu từ finalPrice trong quý
        BigDecimal totalRevenue = ordersRepository.getTotalRevenueFromFinalPriceByQuarter(year, startMonth, endMonth);

        return buildRevenueByModelReport(modelRevenueData, totalRevenue, "QUARTERLY",
                                       year + "-" + String.format("%02d", startMonth) + "-01",
                                       year + "-" + String.format("%02d", endMonth) + "-" +
                                       YearMonth.of(year, endMonth).lengthOfMonth());
    }

    /**
     * Tạo báo cáo doanh thu theo model theo năm
     */
    public RevenueByModelReportResponse generateRevenueByModelYearlyReport(Integer year) {
        // Lấy doanh thu theo model trong năm
        List<Object[]> modelRevenueData = ordersRepository.getRevenueByModelByYear(year);

        // Lấy tổng doanh thu từ finalPrice trong năm
        BigDecimal totalRevenue = ordersRepository.getTotalRevenueFromFinalPriceByYear(year);

        return buildRevenueByModelReport(modelRevenueData, totalRevenue, "YEARLY",
                                       year + "-01-01", year + "-12-31");
    }

    /**
     * Helper method để build response báo cáo doanh thu theo model
     * Trả về tất cả model của đại lý (kể cả model không có doanh thu)
     */
    private RevenueByModelReportResponse buildRevenueByModelReport(List<Object[]> modelRevenueData,
                                                                  BigDecimal totalRevenue,
                                                                  String reportType,
                                                                  String startDate,
                                                                  String endDate) {
        // TODO: Tạm thời sử dụng dealerId = 1, cần thêm tham số dealerId vào các method
        Integer dealerId = 1;

        // Lấy tất cả model của đại lý
        List<Object[]> allModelsData = ordersRepository.getAllModelsByDealer(dealerId);

        // Tạo map từ dữ liệu doanh thu để dễ tra cứu
        Map<Integer, Object[]> revenueMap = new HashMap<>();
        for (Object[] row : modelRevenueData) {
            Integer modelId = (Integer) row[0];
            revenueMap.put(modelId, row);
        }

        List<RevenueByModelReportResponse.ModelRevenueDetail> modelRevenueDetails = new ArrayList<>();
        Long totalCarsSold = 0L;

        // Duyệt qua tất cả model của đại lý
        for (Object[] modelRow : allModelsData) {
            Integer modelId = (Integer) modelRow[0];
            String modelName = (String) modelRow[1];

            // Lấy dữ liệu doanh thu (nếu có)
            Object[] revenueRow = revenueMap.get(modelId);

            Long carsSold = 0L;
            BigDecimal revenue = BigDecimal.ZERO;

            if (revenueRow != null) {
                carsSold = ((Number) revenueRow[2]).longValue();
                revenue = (BigDecimal) revenueRow[3];
                totalCarsSold += carsSold;
            }

            modelRevenueDetails.add(RevenueByModelReportResponse.ModelRevenueDetail.builder()
                    .modelId(modelId)
                    .modelName(modelName)
                    .carsSold(carsSold)
                    .revenue(revenue != null ? revenue : BigDecimal.ZERO)
                    .build());
        }

        return RevenueByModelReportResponse.builder()
                .reportType(reportType)
                .startDate(startDate)
                .endDate(endDate)
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .totalCarsSold(totalCarsSold)
                .modelRevenueDetails(modelRevenueDetails)
                .build();
    }

    /**
     * Tạo báo cáo doanh thu theo staff theo tháng
     */
    public RevenueByStaffReportResponse generateRevenueByStaffMonthlyReport(Integer year, Integer month) {
        // Lấy doanh thu theo staff trong tháng
        List<Object[]> staffRevenueData = ordersRepository.getRevenueByStaffByMonth(year, month);

        // Lấy tổng doanh thu từ finalPrice trong tháng
        BigDecimal totalRevenue = ordersRepository.getTotalRevenueFromFinalPriceByMonth(year, month);

        return buildRevenueByStaffReport(staffRevenueData, totalRevenue, "MONTHLY",
                                       year + "-" + String.format("%02d", month) + "-01",
                                       year + "-" + String.format("%02d", month) + "-" +
                                       YearMonth.of(year, month).lengthOfMonth());
    }

    /**
     * Tạo báo cáo doanh thu theo staff theo quý
     */
    public RevenueByStaffReportResponse generateRevenueByStaffQuarterlyReport(Integer year, Integer quarter) {
        // Tính tháng bắt đầu và kết thúc của quý
        int startMonth = (quarter - 1) * 3 + 1;
        int endMonth = startMonth + 2;

        // Lấy doanh thu theo staff trong quý
        List<Object[]> staffRevenueData = ordersRepository.getRevenueByStaffByQuarter(year, startMonth, endMonth);

        // Lấy tổng doanh thu từ finalPrice trong quý
        BigDecimal totalRevenue = ordersRepository.getTotalRevenueFromFinalPriceByQuarter(year, startMonth, endMonth);

        return buildRevenueByStaffReport(staffRevenueData, totalRevenue, "QUARTERLY",
                                       year + "-" + String.format("%02d", startMonth) + "-01",
                                       year + "-" + String.format("%02d", endMonth) + "-" +
                                       YearMonth.of(year, endMonth).lengthOfMonth());
    }

    /**
     * Tạo báo cáo doanh thu theo staff theo năm
     */
    public RevenueByStaffReportResponse generateRevenueByStaffYearlyReport(Integer year) {
        // Lấy doanh thu theo staff trong năm
        List<Object[]> staffRevenueData = ordersRepository.getRevenueByStaffByYear(year);

        // Lấy tổng doanh thu từ finalPrice trong năm
        BigDecimal totalRevenue = ordersRepository.getTotalRevenueFromFinalPriceByYear(year);

        return buildRevenueByStaffReport(staffRevenueData, totalRevenue, "YEARLY",
                                       year + "-01-01", year + "-12-31");
    }

    /**
     * Helper method để build response báo cáo doanh thu theo staff
     * Trả về tất cả staff của đại lý (kể cả staff không có doanh thu)
     */
    private RevenueByStaffReportResponse buildRevenueByStaffReport(List<Object[]> staffRevenueData,
                                                                  BigDecimal totalRevenue,
                                                                  String reportType,
                                                                  String startDate,
                                                                  String endDate) {
        // TODO: Tạm thời sử dụng dealerId = 1, cần thêm tham số dealerId vào các method
        Integer dealerId = 1;

        // Lấy tất cả staff của đại lý
        List<Object[]> allStaffData = ordersRepository.getAllStaffByDealer(dealerId);

        // Tạo map từ dữ liệu doanh thu để dễ tra cứu
        Map<Integer, Object[]> revenueMap = new HashMap<>();
        for (Object[] row : staffRevenueData) {
            Integer staffId = (Integer) row[0];
            revenueMap.put(staffId, row);
        }

        List<RevenueByStaffReportResponse.StaffRevenueDetail> staffRevenueDetails = new ArrayList<>();
        Long totalCarsSold = 0L;

        // Duyệt qua tất cả staff của đại lý
        for (Object[] staffRow : allStaffData) {
            Integer staffId = (Integer) staffRow[0];
            String staffName = (String) staffRow[1];
            String username = (String) staffRow[2];

            // Lấy dữ liệu doanh thu (nếu có)
            Object[] revenueRow = revenueMap.get(staffId);

            Long carsSold = 0L;
            BigDecimal revenue = BigDecimal.ZERO;

            if (revenueRow != null) {
                carsSold = ((Number) revenueRow[3]).longValue();
                revenue = (BigDecimal) revenueRow[4];
                totalCarsSold += carsSold;
            }

            staffRevenueDetails.add(RevenueByStaffReportResponse.StaffRevenueDetail.builder()
                    .staffId(staffId)
                    .staffName(staffName)
                    .carsSold(carsSold)
                    .revenue(revenue != null ? revenue : BigDecimal.ZERO)
                    .build());
        }

        return RevenueByStaffReportResponse.builder()
                .reportType(reportType)
                .startDate(startDate)
                .endDate(endDate)
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .totalCarsSold(totalCarsSold)
                .staffRevenueDetails(staffRevenueDetails)
                .build();
    }

    /**
     * Tạo báo cáo chi phí nhập xe theo tháng
     */
    public CarImportCostReportResponse generateMonthlyImportCostReport(Integer year, Integer month) {
        List<Object[]> dailyData = carDistributionRequestRepository.findDailyImportCostInMonth(year, month);

        List<CarImportCostReportResponse.ImportCostDetail> importCostDetails = new ArrayList<>();
        BigDecimal totalImportCost = BigDecimal.ZERO;
        Integer totalCarsImported = 0;

        // Lấy số ngày trong tháng
        int daysInMonth = YearMonth.of(year, month).lengthOfMonth();

        // Tạo map để tra cứu dữ liệu theo ngày
        Map<Integer, Object[]> dailyDataMap = new HashMap<>();
        for (Object[] row : dailyData) {
            Integer day = (Integer) row[0];
            dailyDataMap.put(day, row);
        }

        // Tạo chi tiết cho từng ngày trong tháng
        for (int day = 1; day <= daysInMonth; day++) {
            Object[] dayData = dailyDataMap.get(day);
            BigDecimal dayImportCost = BigDecimal.ZERO;
            Integer dayCarsImported = 0;

            if (dayData != null) {
                dayImportCost = (BigDecimal) dayData[1];
                dayCarsImported = ((Number) dayData[2]).intValue();
                totalImportCost = totalImportCost.add(dayImportCost);
                totalCarsImported += dayCarsImported;
            }

            importCostDetails.add(CarImportCostReportResponse.ImportCostDetail.builder()
                    .periodName("Ngày " + day)
                    .periodNumber(day)
                    .importCost(dayImportCost)
                    .carsImported(dayCarsImported)
                    .build());
        }

        return CarImportCostReportResponse.builder()
                .reportType("MONTHLY")
                .year(year)
                .month(month)
                .totalImportCost(totalImportCost)
                .totalCarsImported(totalCarsImported)
                .importCostDetails(importCostDetails)
                .build();
    }

    /**
     * Tạo báo cáo chi phí nhập xe theo quý
     */
    public CarImportCostReportResponse generateQuarterlyImportCostReport(Integer year, Integer quarter) {
        int startMonth = (quarter - 1) * 3 + 1;
        int endMonth = startMonth + 2;

        List<Object[]> monthlyData = carDistributionRequestRepository.findMonthlyImportCostInQuarter(year, startMonth, endMonth);

        List<CarImportCostReportResponse.ImportCostDetail> importCostDetails = new ArrayList<>();
        BigDecimal totalImportCost = BigDecimal.ZERO;
        Integer totalCarsImported = 0;

        // Tạo map để tra cứu dữ liệu theo tháng
        Map<Integer, Object[]> monthlyDataMap = new HashMap<>();
        for (Object[] row : monthlyData) {
            Integer month = (Integer) row[0];
            monthlyDataMap.put(month, row);
        }

        // Tạo chi tiết cho từng tháng trong quý
        for (int month = startMonth; month <= endMonth; month++) {
            Object[] monthData = monthlyDataMap.get(month);
            BigDecimal monthImportCost = BigDecimal.ZERO;
            Integer monthCarsImported = 0;

            if (monthData != null) {
                monthImportCost = (BigDecimal) monthData[1];
                monthCarsImported = ((Number) monthData[2]).intValue();
                totalImportCost = totalImportCost.add(monthImportCost);
                totalCarsImported += monthCarsImported;
            }

            importCostDetails.add(CarImportCostReportResponse.ImportCostDetail.builder()
                    .periodName("Tháng " + month)
                    .periodNumber(month)
                    .importCost(monthImportCost)
                    .carsImported(monthCarsImported)
                    .build());
        }

        return CarImportCostReportResponse.builder()
                .reportType("QUARTERLY")
                .year(year)
                .quarter(quarter)
                .totalImportCost(totalImportCost)
                .totalCarsImported(totalCarsImported)
                .importCostDetails(importCostDetails)
                .build();
    }

    /**
     * Tạo báo cáo chi phí nhập xe theo năm
     */
    public CarImportCostReportResponse generateYearlyImportCostReport(Integer year) {
        List<Object[]> monthlyData = carDistributionRequestRepository.findMonthlyImportCostInYear(year);

        List<CarImportCostReportResponse.ImportCostDetail> importCostDetails = new ArrayList<>();
        BigDecimal totalImportCost = BigDecimal.ZERO;
        Integer totalCarsImported = 0;

        // Tạo map để tra cứu dữ liệu theo tháng
        Map<Integer, Object[]> monthlyDataMap = new HashMap<>();
        for (Object[] row : monthlyData) {
            Integer month = (Integer) row[0];
            monthlyDataMap.put(month, row);
        }

        // Tạo chi tiết cho từng tháng trong năm
        for (int month = 1; month <= 12; month++) {
            Object[] monthData = monthlyDataMap.get(month);
            BigDecimal monthImportCost = BigDecimal.ZERO;
            Integer monthCarsImported = 0;

            if (monthData != null) {
                monthImportCost = (BigDecimal) monthData[1];
                monthCarsImported = ((Number) monthData[2]).intValue();
                totalImportCost = totalImportCost.add(monthImportCost);
                totalCarsImported += monthCarsImported;
            }

            importCostDetails.add(CarImportCostReportResponse.ImportCostDetail.builder()
                    .periodName("Tháng " + month)
                    .periodNumber(month)
                    .importCost(monthImportCost)
                    .carsImported(monthCarsImported)
                    .build());
        }

        return CarImportCostReportResponse.builder()
                .reportType("YEARLY")
                .year(year)
                .totalImportCost(totalImportCost)
                .totalCarsImported(totalCarsImported)
                .importCostDetails(importCostDetails)
                .build();
    }
}
