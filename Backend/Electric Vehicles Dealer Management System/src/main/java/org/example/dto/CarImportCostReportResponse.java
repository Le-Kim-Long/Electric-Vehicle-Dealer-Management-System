package org.example.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarImportCostReportResponse {

    /**
     * Loại báo cáo: MONTHLY, QUARTERLY, YEARLY
     */
    private String reportType;

    /**
     * Năm báo cáo
     */
    private Integer year;

    /**
     * Tháng báo cáo (chỉ có với MONTHLY)
     */
    private Integer month;

    /**
     * Quý báo cáo (chỉ có với QUARTERLY)
     */
    private Integer quarter;

    /**
     * ID của dealer (chỉ có khi báo cáo theo dealer cụ thể)
     */
    private Integer dealerId;

    /**
     * Tổng chi phí nhập xe của kỳ báo cáo
     */
    private BigDecimal totalImportCost;

    /**
     * Tổng số xe nhập của kỳ báo cáo
     */
    private Integer totalCarsImported;

    /**
     * Chi tiết chi phí nhập xe theo từng kỳ con
     */
    private List<ImportCostDetail> importCostDetails;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportCostDetail {
        /**
         * Tên kỳ (Ngày 1, Ngày 2,... cho tháng; Tháng 1, Tháng 2, Tháng 3 cho quý; Tháng 1-12 cho năm)
         */
        private String periodName;

        /**
         * Số thứ tự kỳ (1-31 cho tháng, 1-3 cho quý, 1-12 cho năm)
         */
        private Integer periodNumber;

        /**
         * Chi phí nhập xe của kỳ này
         */
        private BigDecimal importCost;

        /**
         * Số lượng xe nhập của kỳ này
         */
        private Integer carsImported;
    }
}
