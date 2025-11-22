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
     * Tạo báo cáo tổng quát trong khoảng thời gian cho dealer cụ thể
     */
    public SalesReportResponse generateSalesReportForDealer(LocalDateTime startDate, LocalDateTime endDate, Integer dealerId) {

        // Tổng đơn hàng đã thanh toán (tính theo completedDate) cho dealer cụ thể
        Long totalCompletedOrders = ordersRepository.countCompletedOrdersByCompletedDateRangeAndDealer(
            startDate, endDate, dealerId
        );

        // Tổng đơn hàng chưa hoàn thành (tính theo orderDate) cho dealer cụ thể
        Long totalPendingOrders = ordersRepository.countPendingOrdersByOrderDateRangeAndDealer(
            startDate, endDate, dealerId
        );

        // Tổng xe bán được từ đơn hàng đã thanh toán cho dealer cụ thể
        Long totalCarsSold = ordersRepository.countTotalCarsSoldByCompletedDateRangeAndDealer(
            startDate, endDate, dealerId
        );

        // Tổng doanh thu từ đơn hàng đã thanh toán cho dealer cụ thể
        BigDecimal totalRevenue = ordersRepository.sumTotalRevenueByCompletedDateRangeAndDealer(
            startDate, endDate, dealerId
        );

        // Tổng lợi nhuận (dealer price - manufacturer price) cho dealer cụ thể
        BigDecimal totalProfit = ordersRepository.sumTotalProfitByCompletedDateRangeAndDealer(
            startDate, endDate, dealerId
        );

        // Tổng số xe nhập vào từ các yêu cầu phân phối đã giao cho dealer cụ thể
        Long totalCarsDistributed = carDistributionRequestRepository.sumTotalCarsDistributedByDeliveryDateRangeAndDealer(
            startDate, endDate, dealerId
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

    // ===== DEALER-SPECIFIC REVENUE REPORT METHODS =====

    /**
     * Tạo báo cáo doanh thu theo tháng cho dealer cụ thể
     */
    public RevenueReportResponse generateMonthlyRevenueReportForDealer(Integer year, Integer month, Integer dealerId) {
        // Lấy tổng doanh thu của tháng cho dealer cụ thể
        BigDecimal totalRevenue = ordersRepository.getTotalRevenueByMonthForDealer(year, month, dealerId);

        // Lấy doanh thu theo từng ngày trong tháng cho dealer cụ thể
        List<Object[]> dailyRevenueData = ordersRepository.getDailyRevenueByMonthForDealer(year, month, dealerId);

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
     * Tạo báo cáo doanh thu theo quý cho dealer cụ thể
     */
    public RevenueReportResponse generateQuarterlyRevenueReportForDealer(Integer year, Integer quarter, Integer dealerId) {
        // Tính tháng bắt đầu và kết thúc của quý
        int startMonth = (quarter - 1) * 3 + 1;
        int endMonth = startMonth + 2;

        // Lấy tổng doanh thu của quý cho dealer cụ thể
        BigDecimal totalRevenue = ordersRepository.getTotalRevenueByQuarterForDealer(year, startMonth, endMonth, dealerId);

        // Lấy doanh thu theo từng tháng trong quý cho dealer cụ thể
        List<Object[]> monthlyRevenueData = ordersRepository.getMonthlyRevenueByQuarterForDealer(year, startMonth, endMonth, dealerId);

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
     * Tạo báo cáo doanh thu theo năm cho dealer cụ thể
     */
    public RevenueReportResponse generateYearlyRevenueReportForDealer(Integer year, Integer dealerId) {
        // Lấy tổng doanh thu của năm cho dealer cụ thể
        BigDecimal totalRevenue = ordersRepository.getTotalRevenueByYearForDealer(year, dealerId);

        // Lấy doanh thu theo từng tháng trong năm cho dealer cụ thể
        List<Object[]> monthlyRevenueData = ordersRepository.getMonthlyRevenueByYearForDealer(year, dealerId);

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

    // ===== DEALER-SPECIFIC REVENUE BY MODEL REPORT METHODS =====

    /**
     * Tạo báo cáo doanh thu theo model theo tháng cho dealer cụ thể
     */
    public RevenueByModelReportResponse generateRevenueByModelMonthlyReportForDealer(Integer year, Integer month, Integer dealerId) {
        // Lấy doanh thu theo model trong tháng cho dealer cụ thể
        List<Object[]> modelRevenueData = ordersRepository.getRevenueByModelByMonthForDealer(year, month, dealerId);

        // Lấy tổng doanh thu từ finalPrice trong tháng cho dealer cụ thể
        BigDecimal totalRevenue = ordersRepository.getTotalRevenueFromFinalPriceByMonthForDealer(year, month, dealerId);

        return buildRevenueByModelReportForDealer(modelRevenueData, totalRevenue, "MONTHLY",
                                       year + "-" + String.format("%02d", month) + "-01",
                                       year + "-" + String.format("%02d", month) + "-" +
                                       YearMonth.of(year, month).lengthOfMonth(), dealerId);
    }

    /**
     * Tạo báo cáo doanh thu theo model theo quý cho dealer cụ thể
     */
    public RevenueByModelReportResponse generateRevenueByModelQuarterlyReportForDealer(Integer year, Integer quarter, Integer dealerId) {
        // Tính tháng bắt đầu và kết thúc của quý
        int startMonth = (quarter - 1) * 3 + 1;
        int endMonth = startMonth + 2;

        // Lấy doanh thu theo model trong quý cho dealer cụ thể
        List<Object[]> modelRevenueData = ordersRepository.getRevenueByModelByQuarterForDealer(year, startMonth, endMonth, dealerId);

        // Lấy tổng doanh thu từ finalPrice trong quý cho dealer cụ thể
        BigDecimal totalRevenue = ordersRepository.getTotalRevenueFromFinalPriceByQuarterForDealer(year, startMonth, endMonth, dealerId);

        return buildRevenueByModelReportForDealer(modelRevenueData, totalRevenue, "QUARTERLY",
                                       year + "-" + String.format("%02d", startMonth) + "-01",
                                       year + "-" + String.format("%02d", endMonth) + "-" +
                                       YearMonth.of(year, endMonth).lengthOfMonth(), dealerId);
    }

    /**
     * Tạo báo cáo doanh thu theo model theo năm cho dealer cụ thể
     */
    public RevenueByModelReportResponse generateRevenueByModelYearlyReportForDealer(Integer year, Integer dealerId) {
        // Lấy doanh thu theo model trong năm cho dealer cụ thể
        List<Object[]> modelRevenueData = ordersRepository.getRevenueByModelByYearForDealer(year, dealerId);

        // Lấy tổng doanh thu từ finalPrice trong năm cho dealer cụ thể
        BigDecimal totalRevenue = ordersRepository.getTotalRevenueFromFinalPriceByYearForDealer(year, dealerId);

        return buildRevenueByModelReportForDealer(modelRevenueData, totalRevenue, "YEARLY",
                                       year + "-01-01", year + "-12-31", dealerId);
    }

    /**
     * Helper method để build response báo cáo doanh thu theo model cho dealer cụ thể
     * Trả về tất cả model của đại lý (kể cả model không có doanh thu)
     */
    private RevenueByModelReportResponse buildRevenueByModelReportForDealer(List<Object[]> modelRevenueData,
                                                                  BigDecimal totalRevenue,
                                                                  String reportType,
                                                                  String startDate,
                                                                  String endDate,
                                                                  Integer dealerId) {
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

    // ===== DEALER-SPECIFIC REVENUE BY STAFF REPORT METHODS =====

    @Override
    public RevenueByStaffReportResponse generateRevenueByStaffMonthlyReportForDealer(Integer year, Integer month, Integer dealerId) {
        // Lấy doanh thu theo staff trong tháng cho dealer cụ thể
        List<Object[]> staffRevenueData = ordersRepository.getRevenueByStaffByMonthForDealer(year, month, dealerId);

        // Lấy tổng doanh thu từ finalPrice trong tháng cho dealer cụ thể
        BigDecimal totalRevenue = ordersRepository.getTotalRevenueForStaffReportByMonthForDealer(year, month, dealerId);

        return buildRevenueByStaffReportForDealer(staffRevenueData, totalRevenue, "MONTHLY",
                                       year + "-" + String.format("%02d", month) + "-01",
                                       year + "-" + String.format("%02d", month) + "-" +
                                       YearMonth.of(year, month).lengthOfMonth(), dealerId);
    }

    @Override
    public RevenueByStaffReportResponse generateRevenueByStaffQuarterlyReportForDealer(Integer year, Integer quarter, Integer dealerId) {
        // Tính tháng bắt đầu và kết thúc của quý
        int startMonth = (quarter - 1) * 3 + 1;
        int endMonth = startMonth + 2;

        // Lấy doanh thu theo staff trong quý cho dealer cụ thể
        List<Object[]> staffRevenueData = ordersRepository.getRevenueByStaffByQuarterForDealer(year, startMonth, endMonth, dealerId);

        // Lấy tổng doanh thu từ finalPrice trong quý cho dealer cụ thể
        BigDecimal totalRevenue = ordersRepository.getTotalRevenueForStaffReportByQuarterForDealer(year, startMonth, endMonth, dealerId);

        return buildRevenueByStaffReportForDealer(staffRevenueData, totalRevenue, "QUARTERLY",
                                       year + "-" + String.format("%02d", startMonth) + "-01",
                                       year + "-" + String.format("%02d", endMonth) + "-" +
                                       YearMonth.of(year, endMonth).lengthOfMonth(), dealerId);
    }

    @Override
    public RevenueByStaffReportResponse generateRevenueByStaffYearlyReportForDealer(Integer year, Integer dealerId) {
        // Lấy doanh thu theo staff trong năm cho dealer cụ thể
        List<Object[]> staffRevenueData = ordersRepository.getRevenueByStaffByYearForDealer(year, dealerId);

        // Lấy tổng doanh thu từ finalPrice trong năm cho dealer cụ thể
        BigDecimal totalRevenue = ordersRepository.getTotalRevenueForStaffReportByYearForDealer(year, dealerId);

        return buildRevenueByStaffReportForDealer(staffRevenueData, totalRevenue, "YEARLY",
                                       year + "-01-01", year + "-12-31", dealerId);
    }

    /**
     * Helper method để build response báo cáo doanh thu theo staff cho dealer cụ thể
     * Trả về tất cả staff của đại lý (kể cả staff không có doanh thu)
     */
    private RevenueByStaffReportResponse buildRevenueByStaffReportForDealer(List<Object[]> staffRevenueData,
                                                                  BigDecimal totalRevenue,
                                                                  String reportType,
                                                                  String startDate,
                                                                  String endDate,
                                                                  Integer dealerId) {
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

            // Lấy dữ liệu doanh thu (nếu có)
            Object[] revenueRow = revenueMap.get(staffId);

            Long carsSold = 0L;
            BigDecimal revenue = BigDecimal.ZERO;

            if (revenueRow != null) {
                carsSold = ((Number) revenueRow[2]).longValue();
                revenue = (BigDecimal) revenueRow[3];
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
     * Tạo báo cáo chi phí nhập xe theo tháng cho dealer cụ thể
     */
    public CarImportCostReportResponse generateMonthlyImportCostReportForDealer(Integer year, Integer month, Integer dealerId) {
        List<Object[]> dailyData = carDistributionRequestRepository.findDailyImportCostInMonthForDealer(year, month, dealerId);

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
                .dealerId(dealerId)
                .totalImportCost(totalImportCost)
                .totalCarsImported(totalCarsImported)
                .importCostDetails(importCostDetails)
                .build();
    }

    /**
     * Tạo báo cáo chi phí nhập xe theo quý cho dealer cụ thể
     */
    public CarImportCostReportResponse generateQuarterlyImportCostReportForDealer(Integer year, Integer quarter, Integer dealerId) {
        int startMonth = (quarter - 1) * 3 + 1;
        int endMonth = startMonth + 2;

        List<Object[]> monthlyData = carDistributionRequestRepository.findMonthlyImportCostInQuarterForDealer(year, startMonth, endMonth, dealerId);

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
                .dealerId(dealerId)
                .totalImportCost(totalImportCost)
                .totalCarsImported(totalCarsImported)
                .importCostDetails(importCostDetails)
                .build();
    }

    /**
     * Tạo báo cáo chi phí nhập xe theo năm cho dealer cụ thể
     */
    public CarImportCostReportResponse generateYearlyImportCostReportForDealer(Integer year, Integer dealerId) {
        List<Object[]> monthlyData = carDistributionRequestRepository.findMonthlyImportCostInYearForDealer(year, dealerId);

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
                .dealerId(dealerId)
                .totalImportCost(totalImportCost)
                .totalCarsImported(totalCarsImported)
                .importCostDetails(importCostDetails)
                .build();
    }
}
