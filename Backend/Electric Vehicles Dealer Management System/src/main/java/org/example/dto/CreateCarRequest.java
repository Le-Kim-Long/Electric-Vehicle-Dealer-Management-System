package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCarRequest {

    @NotNull(message = "Variant ID is required")
    private Integer variantId;

    @NotNull(message = "Color ID is required")
    private Integer colorId;

    @NotNull(message = "Production year is required")
    @Min(value = 2000, message = "Production year must be at least 2000")
    @Max(value = 2100, message = "Production year must not exceed 2100")
    private Integer productionYear;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be greater than or equal to 0")
    private Long price;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(Available|Sold|Reserved)$",
             message = "Status must be one of: Available, Sold, Reserved")
    private String status;

    private String imagePath;
}

