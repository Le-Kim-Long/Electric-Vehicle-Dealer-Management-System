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
    @Column(name = "config_id")
    private Integer config_id;

    @Column(name = "variant_id")
    private Integer variant_id; // FK to CarVariant

    @Column(name = "battery_capacity")
    private String battery_capacity;

    @Column(name = "range_km")
    private Integer range_km;

    @Column(name = "motor_power")
    private String motor_power;


}
