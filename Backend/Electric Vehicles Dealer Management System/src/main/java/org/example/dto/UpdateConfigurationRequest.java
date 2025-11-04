package org.example.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateConfigurationRequest {

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
