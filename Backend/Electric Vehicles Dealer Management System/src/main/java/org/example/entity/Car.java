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
    @Column(name = "CarId")
    private Integer car_id;

    @Column(name = "VariantId")
    private Integer variant_id;   // FK to CarVariant

    @Column(name = "ColorId")
    private Integer color_id;     // FK to Color

    @Column(name = "ProductionYear")
    private Integer production_year;

    @Column(name = "Price")
    private Long price;


}
