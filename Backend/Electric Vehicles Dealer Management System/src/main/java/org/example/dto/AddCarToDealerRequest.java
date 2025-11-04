package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCarToDealerRequest {
    @NotBlank(message = "Model name is required")
    private String modelName;

    @NotBlank(message = "Variant name is required")
    private String variantName;

    @NotBlank(message = "Color name is required")
    private String colorName;

    @NotBlank(message = "Dealer name is required")
    private String dealerName;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
}
