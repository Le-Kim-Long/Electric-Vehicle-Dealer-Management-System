package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import org.example.dto.RevenueReportResponse;
import org.example.dto.SalesReportResponse;
import org.example.dto.RevenueByModelReportResponse;
import org.example.dto.RevenueByStaffReportResponse;
import org.example.dto.CarImportCostReportResponse;
import org.example.entity.UserAccount;
import org.example.repository.UserAccountRepository;
import org.example.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Sales Report Management", description = "APIs for generating sales reports with role-based access control")
public class ReportController {

    private final ReportService reportService;
    private final UserAccountRepository userAccountRepository;

    public enum PeriodType {
        CUSTOM,    // Tùy chỉnh với startDate và endDate
        MONTHLY,   // Theo tháng
        QUARTERLY, // Theo quý
        YEARLY     // Theo năm
    }

    @GetMapping("/sales")
    @Operation(
        summary = "Tạo báo cáo tổng quát về bán hàng",
        description = "Lấy báo cáo tổng quát bao gồm: tổng đơn hàng đã thanh toán (theo completedDate), " +
                     "tổng đơn hàng chưa hoàn thành (theo orderDate), tổng xe bán được, tổng doanh thu, " +
                     "và tổng lợi nhuận trong khoảng thời gian chỉ định"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy báo cáo thành công"),
        @ApiResponse(responseCode = "400", description = "Tham số không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
        @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public ResponseEntity<SalesReportResponse> getSalesReport(
        @Parameter(description = "Ngày bắt đầu (format: yyyy-MM-dd'T'HH:mm:ss)", required = true)
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,

        @Parameter(description = "Ngày kết thúc (format: yyyy-MM-dd'T'HH:mm:ss)", required = true)
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Ngày bắt đầu không thể sau ngày kết thúc");
        }

        // Lấy authentication từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Lấy email từ authentication (JWT subject)
        String email = authentication.getName();

        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Lấy thông tin user để lấy dealer ID
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        if (user.getDealer() == null) {
            throw new RuntimeException("User not associated with any dealer");
        }

        Integer dealerId = user.getDealer().getDealerId();

        SalesReportResponse report = reportService.generateSalesReportForDealer(startDate, endDate, dealerId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/sales/period")
    @Operation(
        summary = "Tạo báo cáo bán hàng theo kỳ (tháng/quý/năm)",
        description = "Lấy báo cáo bán hàng theo kỳ được chỉ định: tháng, quý, năm hoặc khoảng thời gian tùy chỉnh"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy báo cáo thành công"),
        @ApiResponse(responseCode = "400", description = "Tham số không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
        @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public ResponseEntity<SalesReportResponse> getSalesReportByPeriod(
        @Parameter(description = "Loại kỳ báo cáo: CUSTOM, MONTHLY, QUARTERLY, YEARLY", required = true)
        @RequestParam PeriodType periodType,

        @Parameter(description = "Năm (bắt buộc cho tất cả loại kỳ)", required = true)
        @RequestParam Integer year,

        @Parameter(description = "Tháng (1-12, bắt buộc cho MONTHLY và QUARTERLY)")
        @RequestParam(required = false) Integer month,

        @Parameter(description = "Quý (1-4, chỉ dùng cho QUARTERLY, ưu tiên hơn month)")
        @RequestParam(required = false) Integer quarter,

        @Parameter(description = "Ngày bắt đầu tùy chỉnh (chỉ dùng cho CUSTOM)")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime customStartDate,

        @Parameter(description = "Ngày kết thúc tùy chỉnh (chỉ dùng cho CUSTOM)")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime customEndDate
    ) {
        // Lấy authentication từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Lấy email từ authentication (JWT subject)
        String email = authentication.getName();

        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Lấy thông tin user để lấy dealer ID
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        if (user.getDealer() == null) {
            throw new RuntimeException("User not associated with any dealer");
        }

        Integer dealerId = user.getDealer().getDealerId();

        LocalDateTime startDate;
        LocalDateTime endDate;

        switch (periodType) {
            case CUSTOM:
                if (customStartDate == null || customEndDate == null) {
                    throw new IllegalArgumentException("Với CUSTOM period, cần cung cấp customStartDate và customEndDate");
                }
                startDate = customStartDate;
                endDate = customEndDate;
                break;

            case MONTHLY:
                if (month == null || month < 1 || month > 12) {
                    throw new IllegalArgumentException("Với MONTHLY period, cần cung cấp month hợp lệ (1-12)");
                }
                YearMonth yearMonth = YearMonth.of(year, month);
                startDate = yearMonth.atDay(1).atStartOfDay();
                endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);
                break;

            case QUARTERLY:
                int quarterMonth;
                if (quarter != null) {
                    // Ưu tiên sử dụng quarter nếu được cung cấp
                    if (quarter < 1 || quarter > 4) {
                        throw new IllegalArgumentException("Quarter phải từ 1 đến 4");
                    }
                    quarterMonth = (quarter - 1) * 3 + 1; // Q1=1, Q2=4, Q3=7, Q4=10
                } else if (month != null) {
                    // Tính quarter từ month
                    if (month < 1 || month > 12) {
                        throw new IllegalArgumentException("Month phải từ 1 đến 12");
                    }
                    quarterMonth = ((month - 1) / 3) * 3 + 1;
                } else {
                    throw new IllegalArgumentException("Với QUARTERLY period, cần cung cấp quarter hoặc month");
                }

                YearMonth quarterStart = YearMonth.of(year, quarterMonth);
                YearMonth quarterEnd = YearMonth.of(year, quarterMonth + 2);
                startDate = quarterStart.atDay(1).atStartOfDay();
                endDate = quarterEnd.atEndOfMonth().atTime(23, 59, 59);
                break;

            case YEARLY:
                startDate = LocalDate.of(year, 1, 1).atStartOfDay();
                endDate = LocalDate.of(year, 12, 31).atTime(23, 59, 59);
                break;

            default:
                throw new IllegalArgumentException("Loại period không được hỗ trợ: " + periodType);
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Ngày bắt đầu không thể sau ngày kết thúc");
        }

        // Sử dụng service method mới để báo cáo cho dealer cụ thể
        SalesReportResponse report = reportService.generateSalesReportForDealer(startDate, endDate, dealerId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/revenue/monthly")
    @Operation(
        summary = "Báo cáo doanh thu theo tháng cho dealer hiện tại",
        description = "Lấy báo cáo doanh thu chi tiết theo từng ngày trong tháng từ các đơn hàng đã thanh toán của dealer mà tài khoản đang đăng nhập"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy báo cáo thành công"),
        @ApiResponse(responseCode = "400", description = "Tham số không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
        @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public ResponseEntity<RevenueReportResponse> getMonthlyRevenueReport(
        @Parameter(description = "Năm (ví dụ: 2025)", required = true)
        @RequestParam Integer year,

        @Parameter(description = "Tháng (1-12)", required = true)
        @RequestParam Integer month
    ) {
        if (year < 1900 || year > 2100) {
            throw new IllegalArgumentException("Năm phải trong khoảng từ 1900 đến 2100");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Tháng phải từ 1 đến 12");
        }

        // Lấy authentication từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Lấy email từ authentication (JWT subject)
        String email = authentication.getName();

        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Lấy thông tin user để lấy dealer ID
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        if (user.getDealer() == null) {
            throw new RuntimeException("User not associated with any dealer");
        }

        Integer dealerId = user.getDealer().getDealerId();

        RevenueReportResponse report = reportService.generateMonthlyRevenueReportForDealer(year, month, dealerId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/revenue/quarterly")
    @Operation(
        summary = "Báo cáo doanh thu theo quý cho dealer hiện tại",
        description = "Lấy báo cáo doanh thu chi tiết theo từng tháng trong quý từ các đơn hàng đã thanh toán của dealer mà tài khoản đang đăng nhập"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy báo cáo thành công"),
        @ApiResponse(responseCode = "400", description = "Tham số không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
        @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public ResponseEntity<RevenueReportResponse> getQuarterlyRevenueReport(
        @Parameter(description = "Năm (ví dụ: 2025)", required = true)
        @RequestParam Integer year,

        @Parameter(description = "Quý (1-4)", required = true)
        @RequestParam Integer quarter
    ) {
        if (year < 1900 || year > 2100) {
            throw new IllegalArgumentException("Năm phải trong khoảng từ 1900 đến 2100");
        }
        if (quarter < 1 || quarter > 4) {
            throw new IllegalArgumentException("Quý phải từ 1 đến 4");
        }

        // Lấy authentication từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Lấy email từ authentication (JWT subject)
        String email = authentication.getName();

        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Lấy thông tin user để lấy dealer ID
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        if (user.getDealer() == null) {
            throw new RuntimeException("User not associated with any dealer");
        }

        Integer dealerId = user.getDealer().getDealerId();

        RevenueReportResponse report = reportService.generateQuarterlyRevenueReportForDealer(year, quarter, dealerId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/revenue/yearly")
    @Operation(
        summary = "Báo cáo doanh thu theo năm cho dealer hiện tại",
        description = "Lấy báo cáo doanh thu chi tiết theo từng tháng trong năm từ các đơn hàng đã thanh toán của dealer mà tài khoản đang đăng nhập"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy báo cáo thành công"),
        @ApiResponse(responseCode = "400", description = "Tham số không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
        @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public ResponseEntity<RevenueReportResponse> getYearlyRevenueReport(
        @Parameter(description = "Năm (ví dụ: 2025)", required = true)
        @RequestParam Integer year
    ) {
        if (year < 1900 || year > 2100) {
            throw new IllegalArgumentException("Năm phải trong khoảng từ 1900 đến 2100");
        }

        // Lấy authentication từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Lấy email từ authentication (JWT subject)
        String email = authentication.getName();

        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Lấy thông tin user để lấy dealer ID
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        if (user.getDealer() == null) {
            throw new RuntimeException("User not associated with any dealer");
        }

        Integer dealerId = user.getDealer().getDealerId();

        RevenueReportResponse report = reportService.generateYearlyRevenueReportForDealer(year, dealerId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/revenue/model/monthly")
    @Operation(
        summary = "Báo cáo doanh thu theo model xe trong tháng cho dealer hiện tại",
        description = "Lấy báo cáo doanh thu chi tiết theo từng model xe trong tháng từ các đơn hàng đã thanh toán của dealer mà tài khoản đang đăng nhập"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy báo cáo thành công"),
        @ApiResponse(responseCode = "400", description = "Tham số không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
        @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public ResponseEntity<RevenueByModelReportResponse> getRevenueByModelMonthlyReport(
        @Parameter(description = "Năm (ví dụ: 2025)", required = true)
        @RequestParam Integer year,

        @Parameter(description = "Tháng (1-12)", required = true)
        @RequestParam Integer month
    ) {
        if (year < 1900 || year > 2100) {
            throw new IllegalArgumentException("Năm phải trong khoảng từ 1900 đến 2100");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Tháng phải từ 1 đến 12");
        }

        // Lấy authentication từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Lấy email từ authentication (JWT subject)
        String email = authentication.getName();

        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Lấy thông tin user để lấy dealer ID
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        if (user.getDealer() == null) {
            throw new RuntimeException("User not associated with any dealer");
        }

        Integer dealerId = user.getDealer().getDealerId();

        RevenueByModelReportResponse report = reportService.generateRevenueByModelMonthlyReportForDealer(year, month, dealerId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/revenue/model/quarterly")
    @Operation(
        summary = "Báo cáo doanh thu theo model xe trong quý cho dealer hiện tại",
        description = "Lấy báo cáo doanh thu chi tiết theo từng model xe trong quý từ các đơn hàng đã thanh toán của dealer mà tài khoản đang đăng nhập"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy báo cáo thành công"),
        @ApiResponse(responseCode = "400", description = "Tham số không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
        @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public ResponseEntity<RevenueByModelReportResponse> getRevenueByModelQuarterlyReport(
        @Parameter(description = "Năm (ví dụ: 2025)", required = true)
        @RequestParam Integer year,

        @Parameter(description = "Quý (1-4)", required = true)
        @RequestParam Integer quarter
    ) {
        if (year < 1900 || year > 2100) {
            throw new IllegalArgumentException("Năm phải trong khoảng từ 1900 đến 2100");
        }
        if (quarter < 1 || quarter > 4) {
            throw new IllegalArgumentException("Quý phải từ 1 đến 4");
        }

        // Lấy authentication từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Lấy email từ authentication (JWT subject)
        String email = authentication.getName();

        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Lấy thông tin user để lấy dealer ID
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        if (user.getDealer() == null) {
            throw new RuntimeException("User not associated with any dealer");
        }

        Integer dealerId = user.getDealer().getDealerId();

        RevenueByModelReportResponse report = reportService.generateRevenueByModelQuarterlyReportForDealer(year, quarter, dealerId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/revenue/model/yearly")
    @Operation(
        summary = "Báo cáo doanh thu theo model xe trong năm cho dealer hiện tại",
        description = "Lấy báo cáo doanh thu chi tiết theo từng model xe trong năm từ các đơn hàng đã thanh toán của dealer mà tài khoản đang đăng nhập"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy báo cáo thành công"),
        @ApiResponse(responseCode = "400", description = "Tham số không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
        @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public ResponseEntity<RevenueByModelReportResponse> getRevenueByModelYearlyReport(
        @Parameter(description = "Năm (ví dụ: 2025)", required = true)
        @RequestParam Integer year
    ) {
        if (year < 1900 || year > 2100) {
            throw new IllegalArgumentException("Năm phải trong khoảng từ 1900 đến 2100");
        }

        // Lấy authentication từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Lấy email từ authentication (JWT subject)
        String email = authentication.getName();

        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Lấy thông tin user để lấy dealer ID
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        if (user.getDealer() == null) {
            throw new RuntimeException("User not associated with any dealer");
        }

        Integer dealerId = user.getDealer().getDealerId();

        RevenueByModelReportResponse report = reportService.generateRevenueByModelYearlyReportForDealer(year, dealerId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/revenue/staff/monthly")
    @Operation(
        summary = "Báo cáo doanh thu theo nhân viên trong tháng cho dealer hiện tại",
        description = "Lấy báo cáo doanh thu chi tiết theo từng nhân viên trong tháng từ các đơn hàng đã thanh toán của dealer mà tài khoản đang đăng nhập"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy báo cáo thành công"),
        @ApiResponse(responseCode = "400", description = "Tham số không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
        @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public ResponseEntity<RevenueByStaffReportResponse> getRevenueByStaffMonthlyReport(
        @Parameter(description = "Năm (ví dụ: 2025)", required = true)
        @RequestParam Integer year,

        @Parameter(description = "Tháng (1-12)", required = true)
        @RequestParam Integer month
    ) {
        if (year < 1900 || year > 2100) {
            throw new IllegalArgumentException("Năm phải trong khoảng từ 1900 đến 2100");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Tháng phải từ 1 đến 12");
        }

        // Lấy authentication từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Lấy email từ authentication (JWT subject)
        String email = authentication.getName();

        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Lấy thông tin user để lấy dealer ID
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        if (user.getDealer() == null) {
            throw new RuntimeException("User not associated with any dealer");
        }

        Integer dealerId = user.getDealer().getDealerId();

        RevenueByStaffReportResponse report = reportService.generateRevenueByStaffMonthlyReportForDealer(year, month, dealerId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/revenue/staff/quarterly")
    @Operation(
        summary = "Báo cáo doanh thu theo nhân viên trong quý cho dealer hiện tại",
        description = "Lấy báo cáo doanh thu chi tiết theo từng nhân viên trong quý từ các đơn hàng đã thanh toán của dealer mà tài khoản đang đăng nhập"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy báo cáo thành công"),
        @ApiResponse(responseCode = "400", description = "Tham số không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
        @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public ResponseEntity<RevenueByStaffReportResponse> getRevenueByStaffQuarterlyReport(
        @Parameter(description = "Năm (ví dụ: 2025)", required = true)
        @RequestParam Integer year,

        @Parameter(description = "Quý (1-4)", required = true)
        @RequestParam Integer quarter
    ) {
        if (year < 1900 || year > 2100) {
            throw new IllegalArgumentException("Năm phải trong khoảng từ 1900 đến 2100");
        }
        if (quarter < 1 || quarter > 4) {
            throw new IllegalArgumentException("Quý phải từ 1 đến 4");
        }

        // Lấy authentication từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Lấy email từ authentication (JWT subject)
        String email = authentication.getName();

        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Lấy thông tin user để lấy dealer ID
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        if (user.getDealer() == null) {
            throw new RuntimeException("User not associated with any dealer");
        }

        Integer dealerId = user.getDealer().getDealerId();

        RevenueByStaffReportResponse report = reportService.generateRevenueByStaffQuarterlyReportForDealer(year, quarter, dealerId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/revenue/staff/yearly")
    @Operation(
        summary = "Báo cáo doanh thu theo nhân viên trong năm cho dealer hiện tại",
        description = "Lấy báo cáo doanh thu chi tiết theo từng nhân viên trong năm từ các đơn hàng đã thanh toán của dealer mà tài khoản đang đăng nhập"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy báo cáo thành công"),
        @ApiResponse(responseCode = "400", description = "Tham số không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
        @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public ResponseEntity<RevenueByStaffReportResponse> getRevenueByStaffYearlyReport(
        @Parameter(description = "Năm (ví dụ: 2025)", required = true)
        @RequestParam Integer year
    ) {
        if (year < 1900 || year > 2100) {
            throw new IllegalArgumentException("Năm phải trong khoảng từ 1900 đến 2100");
        }

        // Lấy authentication từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Lấy email từ authentication (JWT subject)
        String email = authentication.getName();

        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Lấy thông tin user để lấy dealer ID
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        if (user.getDealer() == null) {
            throw new RuntimeException("User not associated with any dealer");
        }

        Integer dealerId = user.getDealer().getDealerId();

        RevenueByStaffReportResponse report = reportService.generateRevenueByStaffYearlyReportForDealer(year, dealerId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/import-cost/monthly")
    @Operation(
        summary = "Báo cáo chi phí nhập xe theo tháng cho dealer hiện tại",
        description = "Lấy báo cáo chi phí nhập xe chi tiết theo từng ngày trong tháng từ các yêu cầu nhập xe đã giao của dealer mà tài khoản đang đăng nhập"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy báo cáo thành công"),
        @ApiResponse(responseCode = "400", description = "Tham số không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
        @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public ResponseEntity<CarImportCostReportResponse> getMonthlyImportCostReport(
        @Parameter(description = "Năm (ví dụ: 2025)", required = true)
        @RequestParam Integer year,

        @Parameter(description = "Tháng (1-12)", required = true)
        @RequestParam Integer month
    ) {
        if (year < 1900 || year > 2100) {
            throw new IllegalArgumentException("Năm phải trong khoảng từ 1900 đến 2100");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Tháng phải từ 1 đến 12");
        }

        // Lấy authentication từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Lấy email từ authentication (JWT subject)
        String email = authentication.getName();

        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Lấy thông tin user để lấy dealer ID
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        if (user.getDealer() == null) {
            throw new RuntimeException("User not associated with any dealer");
        }

        Integer dealerId = user.getDealer().getDealerId();

        CarImportCostReportResponse report = reportService.generateMonthlyImportCostReportForDealer(year, month, dealerId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/import-cost/quarterly")
    @Operation(
        summary = "Báo cáo chi phí nhập xe theo quý cho dealer hiện tại",
        description = "Lấy báo cáo chi phí nhập xe chi tiết theo từng tháng trong quý từ các yêu cầu nhập xe đã giao của dealer mà tài khoản đang đăng nhập"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy báo cáo thành công"),
        @ApiResponse(responseCode = "400", description = "Tham số không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
        @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public ResponseEntity<CarImportCostReportResponse> getQuarterlyImportCostReport(
        @Parameter(description = "Năm (ví dụ: 2025)", required = true)
        @RequestParam Integer year,

        @Parameter(description = "Quý (1-4)", required = true)
        @RequestParam Integer quarter
    ) {
        if (year < 1900 || year > 2100) {
            throw new IllegalArgumentException("Năm phải trong khoảng từ 1900 đến 2100");
        }
        if (quarter < 1 || quarter > 4) {
            throw new IllegalArgumentException("Quý phải từ 1 đến 4");
        }

        // Lấy authentication từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Lấy email từ authentication (JWT subject)
        String email = authentication.getName();

        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Lấy thông tin user để lấy dealer ID
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        if (user.getDealer() == null) {
            throw new RuntimeException("User not associated with any dealer");
        }

        Integer dealerId = user.getDealer().getDealerId();

        CarImportCostReportResponse report = reportService.generateQuarterlyImportCostReportForDealer(year, quarter, dealerId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/import-cost/yearly")
    @Operation(
        summary = "Báo cáo chi phí nhập xe theo năm cho dealer hiện tại",
        description = "Lấy báo cáo chi phí nhập xe chi tiết theo từng tháng trong năm từ các yêu cầu nhập xe đã giao của dealer mà tài khoản đang đăng nhập"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy báo cáo thành công"),
        @ApiResponse(responseCode = "400", description = "Tham số không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
        @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public ResponseEntity<CarImportCostReportResponse> getYearlyImportCostReport(
        @Parameter(description = "Năm (ví dụ: 2025)", required = true)
        @RequestParam Integer year
    ) {
        if (year < 1900 || year > 2100) {
            throw new IllegalArgumentException("Năm phải trong khoảng từ 1900 đến 2100");
        }

        // Lấy authentication từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Lấy email từ authentication (JWT subject)
        String email = authentication.getName();

        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Lấy thông tin user để lấy dealer ID
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        if (user.getDealer() == null) {
            throw new RuntimeException("User not associated with any dealer");
        }

        Integer dealerId = user.getDealer().getDealerId();

        CarImportCostReportResponse report = reportService.generateYearlyImportCostReportForDealer(year, dealerId);
        return ResponseEntity.ok(report);
    }
}
