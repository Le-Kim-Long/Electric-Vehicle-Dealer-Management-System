package org.example.dto;

import lombok.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateManufacturerPriceRequest {

    @NotNull(message = "Manufacturer price is required")
    @DecimalMin(value = "0", message = "Manufacturer price must be greater than or equal to 0")
    private Long manufacturerPrice;
}
