package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request object for updating payment method")
public class UpdatePaymentMethodRequest {

    @NotBlank(message = "Payment method is required")
    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    @Schema(description = "New payment method", example = "Credit Card", required = true)
    private String method;

    @Size(max = 250, message = "Note must not exceed 250 characters")
    @Schema(description = "Updated payment note", example = "Changed to credit card payment")
    private String note;
}
