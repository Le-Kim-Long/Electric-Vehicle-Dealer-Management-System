package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request object for creating a payment")
public class CreatePaymentRequest {

    @NotNull(message = "Order ID is required")
    @Positive(message = "Order ID must be positive")
    @Schema(description = "Order ID for the payment", example = "1", required = true)
    private Integer orderId;

    @NotBlank(message = "Payment method is required")
    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    @Schema(description = "Payment method", example = "Bank Transfer", required = true)
    private String method;

    @Size(max = 250, message = "Note must not exceed 250 characters")
    @Schema(description = "Payment note", example = "First installment payment")
    private String note;
}
