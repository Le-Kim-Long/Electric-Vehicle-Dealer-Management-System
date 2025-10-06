package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "CONFIGURATION")
public class Configuration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ConfigId")
    private Integer configId;

    @Column(name = "VariantId")
    private Integer variantId; // FK to CarVariant

    @Column(name = "BatteryCapacity")
    private String batteryCapacity;

    @Column(name = "BatteryType")
    private Integer batteryType;

    @Column(name = "FullChargeTime")
    private int fullChargeTime;

    @Column(name = "RangeKm")
    private int rangeKm;

    @Column(name = "Power")
    private String power;

    @Column(name = "Torque")
    private String torque;

    @Column(name = "LengthMm")
    private int lengthMm;

    @Column(name = "WidthMm")
    private int widthMm;

    @Column(name = "HeightMm")
    private int heightMm;

    @Column(name = "WheelbaseMm")
    private int wheelbaseMm;

    @Column(name = "WeightKg")
    private int weightKg;

    @Column(name = "TrunkVolumeL")
    private int trunkVolumeL;

    @Column(name = "Seats")
    private int seats;

}
