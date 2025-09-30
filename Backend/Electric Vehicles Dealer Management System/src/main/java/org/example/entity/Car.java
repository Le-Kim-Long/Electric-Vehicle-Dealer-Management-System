package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "CAR")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "car_id")
    private Integer car_id;

    @Column(name = "variant_id")
    private Integer variant_id;   // FK to CarVariant

    @Column(name = "color_id")
    private Integer color_id;     // FK to Color

    @Column(name = "dealer_id")
    private Integer dealer_id;    // FK to Dealer

    @Column(name = "vin", unique = true, length = 100)
    private String vin;


}
