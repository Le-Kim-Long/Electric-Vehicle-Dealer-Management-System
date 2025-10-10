package org.example.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurationResponse {
    private Integer configId;
    private Integer variantId;
    private String modelName;
    private String variantName;
    private String batteryCapacity;
    private String batteryType;
    private Integer fullChargeTime;
    private Integer rangeKm;
    private Double power;
    private String torque;
    private Integer lengthMm;
    private Integer widthMm;
    private Integer heightMm;
    private Integer wheelbaseMm;
    private Integer weightKg;
    private Integer seats;
}

