package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response object for created installment plan")
public class CreateInstallmentResponse {

    @Schema(description = "Installment plan ID", example = "1")
    private Integer installmentId;

    @Schema(description = "Order ID", example = "1")
    private Integer orderId;

    @Schema(description = "Principal amount (from order total amount)", example = "500000000.00")
    private BigDecimal principalAmount;

    @Schema(description = "Number of installment terms", example = "12")
    private Integer termCount;

    @Schema(description = "Annual interest rate in percentage", example = "8.50")
    private BigDecimal interestRate;

    @Schema(description = "Total interest amount to be paid", example = "22500000.00")
    private BigDecimal totalInterest;

    @Schema(description = "Total amount to be paid (principal + interest)", example = "522500000.00")
    private BigDecimal totalPay;

    @Schema(description = "Amount to pay per term", example = "43541666.67")
    private BigDecimal amountPerTerm;

    @Schema(description = "Additional notes", example = "Special promotion for first-time buyers")
    private String note;

    @Schema(description = "Success message", example = "Installment plan created successfully")
    private String message;
}
