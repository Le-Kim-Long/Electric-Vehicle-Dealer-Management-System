package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateDealerCarRequest {
    private Integer dealerId;
    private Integer carId;
    private BigDecimal dealerPrice;
    private String status; // "Pending", "On Sale", "Sold"
}
