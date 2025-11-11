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
    private Integer variantId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VariantId", insertable = false, updatable = false)
    private CarVariant carVariant;

    @Column(name = "BatteryCapacity")
    private Double batteryCapacity;

    @Column(name = "BatteryType")
    private String batteryType;

    @Column(name = "FullChargeTime")
    private int fullChargeTime;

    @Column(name = "RangeKm")
    private int rangeKm;

    @Column(name = "Power")
    private Double power;

    @Column(name = "Torque")
    private Double torque;

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

    @Column(name="TrunkVolumeL")
    private int trunkVolumeL;

    @Column(name = "Seats")
    private int seats;
}