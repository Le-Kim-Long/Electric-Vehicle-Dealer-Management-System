package org.example.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateOrderPromotionRequest {
    private Integer promotionId; // Optional - có thể null để xóa promotion
}
