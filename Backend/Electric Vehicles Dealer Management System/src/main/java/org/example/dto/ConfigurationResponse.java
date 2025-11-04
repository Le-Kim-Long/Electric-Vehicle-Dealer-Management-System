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
    private Double batteryCapacity;
    private String batteryType;
    private Integer fullChargeTime;
    private Integer rangeKm;
    private Double power;
    private Double torque;
    private Integer lengthMm;
    private Integer widthMm;
    private Integer heightMm;
    private Integer wheelbaseMm;
    private Integer weightKg;
    private Integer trunkVolumeL;
    private Integer seats;
}
