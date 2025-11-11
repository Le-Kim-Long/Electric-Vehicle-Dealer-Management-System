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
public class DealerCarResponse {
    private Integer dealerId;
    private Integer carId;
    private Integer quantity;
    private BigDecimal dealerPrice;
    private String status;

    // Car information
    private String modelName;
    private String variantName;
    private String colorName;
    private Integer productionYear;
    private BigDecimal manufacturerPrice; // Giá niêm yết từ hãng
    private String imagePath;
}
