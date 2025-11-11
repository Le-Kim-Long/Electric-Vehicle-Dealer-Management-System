package org.example.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarResponse {
    private Integer carId;
    private String modelName;
    private String segment;
    private String variantName;
    private String color;
    private Long price;
    private Integer rangeKm;
    private Integer power;
    private Integer quantity;
    private String imagePath;
}
