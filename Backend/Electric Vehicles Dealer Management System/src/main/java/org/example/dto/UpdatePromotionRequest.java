package org.example.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePromotionRequest {

    private String promotionName;
    private String description;
    private BigDecimal value;
    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
}
