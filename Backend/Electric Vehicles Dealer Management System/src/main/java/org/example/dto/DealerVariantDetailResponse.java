package org.example.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealerVariantDetailResponse {

    private Integer variantId;
    private String modelName;
    private String variantName;
    private List<DealerColorPrice> colorPrices;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DealerColorPrice {
        private String colorName;
        private BigDecimal manufacturerPrice; // Giá niêm yết từ hãng - chỉ Dealer Manager xem được
        private BigDecimal dealerPrice; // Giá bán tại dealer - cả Manager và Staff đều xem được
        private String imagePath;
        private Integer quantity;
        private String status; // Trường status cho dealer

        // Method để tạo DealerColorPrice cho Dealer Staff (không có manufacturer price)
        public static DealerColorPrice forDealerStaff(String colorName, BigDecimal dealerPrice,
                                                     String imagePath, Integer quantity, String status) {
            return DealerColorPrice.builder()
                    .colorName(colorName)
                    .manufacturerPrice(null) // Dealer Staff không xem được giá niêm yết
                    .dealerPrice(dealerPrice)
                    .imagePath(imagePath)
                    .quantity(quantity)
                    .status(status)
                    .build();
        }

        // Method để tạo DealerColorPrice cho Dealer Manager (có cả manufacturer price)
        public static DealerColorPrice forDealerManager(String colorName, BigDecimal manufacturerPrice,
                                                       BigDecimal dealerPrice, String imagePath,
                                                       Integer quantity, String status) {
            return DealerColorPrice.builder()
                    .colorName(colorName)
                    .manufacturerPrice(manufacturerPrice) // Dealer Manager xem được cả giá niêm yết
                    .dealerPrice(dealerPrice)
                    .imagePath(imagePath)
                    .quantity(quantity)
                    .status(status)
                    .build();
        }
    }
}
