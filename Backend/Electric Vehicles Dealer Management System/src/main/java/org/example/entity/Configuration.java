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
    private Integer config_id;

    @Column(name = "VariantId")
    private Integer variant_id; // FK to CarVariant

    @Column(name = "BatteryCapacity")
    private String battery_capacity;

    @Column(name = "BatteryType")
    private Integer battery_type;

    @Column(name = "FullChargeTime")
    private int full_charge_time;

    @Column(name = "RangeKm")
    private int range_km;

    @Column(name = "Power")
    private String power;

    @Column(name = "Torque")
    private String torque;

    @Column(name = "LengthMm")
    private int length_mm;

    @Column(name = "WidthMm")
    private int width_mm;

    @Column(name = "HeightMm")
    private int height_mm;

    @Column(name = "WheelbaseMm")
    private int wheelbase_mm;

    @Column(name = "WeightKg")
    private int weight_kg;

    @Column(name = "TrunkVolumL")
    private int trunk_volume_l;

    @Column(name = "Seats")
    private int seats;

}
