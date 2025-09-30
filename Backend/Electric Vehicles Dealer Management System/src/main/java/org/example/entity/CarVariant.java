package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "CAR_VARIANT")
public class CarVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variant_id")
    private Integer variant_id;

    @Column(name = "variant_name", nullable = false, length = 100)
    private String variant_name;

    @Column(name = "model_id")
    private Integer model_id;   // FK to CarModel

    @Column(name = "price")
    private Double price;

}
