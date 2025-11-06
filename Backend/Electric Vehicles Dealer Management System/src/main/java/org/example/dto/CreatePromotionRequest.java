package org.example.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreatePromotionRequest {

    @NotBlank(message = "Promotion name is required")
    private String promotionName;

    private String description;

    @NotNull(message = "Value is required")
    private BigDecimal value;

    @NotBlank(message = "Type is required")
    private String type;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;
}
