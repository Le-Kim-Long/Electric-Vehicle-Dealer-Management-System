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
    private Integer carId;

    @Column(name = "VariantId")
    private Integer variantId;   // FK to CarVariant

    @Column(name = "ColorId")
    private Integer colorId;     // FK to Color

    @Column(name = "ProductionYear")
    private Integer productionYear;

    @Column(name = "Price")
    private Long price;


}
