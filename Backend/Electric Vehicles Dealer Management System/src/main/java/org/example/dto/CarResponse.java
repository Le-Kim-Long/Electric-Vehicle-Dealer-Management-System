package org.example.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CarResponse {
    private Integer carId;
    private Integer variantId;
    private Integer colorId;
    private Integer productionYear;
    private Long price;
}
