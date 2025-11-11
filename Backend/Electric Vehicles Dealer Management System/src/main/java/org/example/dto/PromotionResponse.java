package org.example.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromotionResponse {

    private Integer promotionId;
    private String promotionName;
    private String description;
    private BigDecimal value;
    private String type;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String dealerName;
    private Integer dealerId;
}
