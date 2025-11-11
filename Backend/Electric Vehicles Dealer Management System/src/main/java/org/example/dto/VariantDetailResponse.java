package org.example.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantDetailResponse {

    private Integer variantId;
    private String modelName;
    private String variantName;
    private List<ColorPrice> colorPrices;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ColorPrice {
        private String colorName;
        private Long manufacturerPrice;
        private String imagePath;
        private Integer quantity;
    }
}
