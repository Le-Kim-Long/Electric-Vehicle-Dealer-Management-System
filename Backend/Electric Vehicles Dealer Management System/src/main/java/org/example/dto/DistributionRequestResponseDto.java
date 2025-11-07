package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DistributionRequestResponseDto {

    private Integer requestId;
    private String dealerName;
    private String modelName;
    private String variantName;
    private String colorName;
    private Integer quantity;
    private LocalDateTime requestDate;
    private String status;
    private LocalDateTime approvedDate;
    private LocalDateTime expectedDeliveryDate;
    private LocalDateTime actualDeliveryDate;
}
