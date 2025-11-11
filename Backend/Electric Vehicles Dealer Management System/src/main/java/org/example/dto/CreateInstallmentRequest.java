package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request object for creating an installment plan")
public class CreateInstallmentRequest {

    @NotNull(message = "Order ID is required")
    @Positive(message = "Order ID must be positive")
    @Schema(description = "Order ID for the installment plan", example = "1", required = true)
    private Integer orderId;

    @NotNull(message = "Term count is required")
    @Min(value = 6, message = "Term count must be at least 6 months")
    @Max(value = 60, message = "Term count must not exceed 60 months")
    @Schema(description = "Number of installment terms (months)", example = "12", required = true)
    private Integer termCount;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Interest rate must be non-negative")
    @DecimalMax(value = "50.0", inclusive = true, message = "Interest rate must not exceed 50%")
    @Digits(integer = 2, fraction = 2, message = "Interest rate must have at most 2 decimal places")
    @Schema(description = "Annual interest rate in percentage", example = "8.50", required = true)
    private BigDecimal interestRate;

    @Schema(description = "Additional notes for the installment plan", example = "Special promotion for first-time buyers")
    private String note;
}
